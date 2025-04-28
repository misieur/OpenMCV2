package fr.openmc.core.features.accountdetection;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.openmc.core.CommandsManager;
import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.accountdetection.commands.AccountDetectionCommand;
import fr.openmc.core.utils.DiscordWebhook;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountDetectionManager implements Listener {
    @Getter
    private static AccountDetectionManager instance;
    private final BiMap<String, UUID> ipMap = HashBiMap.create();
    private final OMCPlugin plugin;
    private final File configFile;
    private boolean isAntiVpnEnabled;
    private boolean isAntiDoubleAccountEnabled;
    private Set<UUID> exemptedPlayers;
    private String webhookUrl;

    public AccountDetectionManager(OMCPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.configFile = new File(OMCPlugin.getInstance().getDataFolder() + "/data", "accountdetection.yml");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        CommandsManager.getHandler().register(new AccountDetectionCommand());
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            OMCPlugin.getInstance().saveResource("data/accountdetection.yml", false);
        }
        reload();
    }

    public void reload() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        isAntiVpnEnabled = config.getBoolean("anti-vpn");
        isAntiDoubleAccountEnabled = config.getBoolean("anti-double-account");
        webhookUrl = config.getString("discord-webhook");
        exemptedPlayers = config.getStringList("exempted-players").stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    public void addExemptedPlayer(UUID playersUUID) throws IOException {
        exemptedPlayers.add(playersUUID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("exempted-players", exemptedPlayers);
        config.save(configFile);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = Objects.requireNonNull(player.getAddress()).getHostString();
        if (exemptedPlayers.contains(player.getUniqueId())) return;
        if (isAntiDoubleAccountEnabled) {
            if (ipMap.containsKey(ip) && !ipMap.get(ip).equals(player.getUniqueId())) {
                handleDoubleAccount(Bukkit.getOfflinePlayer(ipMap.get(ip)), player);
            } else if (!ipMap.containsKey(ip)) {
                ipMap.inverse().put(player.getUniqueId(), ip);
            }
        }
        if (isAntiVpnEnabled) verifyIpAddress(ip, player);
    }

    private void handleDoubleAccount(OfflinePlayer firstPlayer, Player secondPlayer) {
        secondPlayer.sendMessage("§cOn dirait que vous utilisez un double compte, nous tenons à rappeler que cela est strictement interdit, §naucune sanction ne vous est donnée pour le moment§r§c, des modérateurs appliquerons une sanction si c'est bien le cas.");
        plugin.getLogger().warning("Double compte détecté: " + firstPlayer.getName() + " et " + secondPlayer.getName());
        try {
            DiscordWebhook.sendMessage(webhookUrl, "Double compte détecté: " + firstPlayer.getName() + " et " + secondPlayer.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible d'envoyer le message sur Discord: " + e.getMessage());
        }
    }

    private void handleVpn(Player player) {
        player.sendMessage("§cOn dirait que vous utilisez un VPN, nous tenons à vous rappeler que cela est strictement interdit, des modérateurs vont s'en charger, §naucune sanction ne vous est donnée pour le moment§r§c.");
        plugin.getLogger().warning("Vpn détecté: " + player.getName() + " plus d'info: https://api.ipapi.is/?q=" + Objects.requireNonNull(player.getAddress()).getHostString());
        try {
            DiscordWebhook.sendMessage(webhookUrl, "Vpn détecté: " + player.getName() + " plus d'info: ||https://api.ipapi.is/?q=" + Objects.requireNonNull(player.getAddress()).getHostString() + "||");
            // L'adresse ip avec un spoiler mais vu que c'est un VPN on s'en fout de leak aux modos l'Ip c'est sa faute. ;)
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible d'envoyer le message sur Discord: " + e.getMessage());
        }
    }

    private void verifyIpAddress(String ip, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpsURLConnection con = (HttpsURLConnection) new URI("https://de.ipapi.is/?q=" + ip)
                            .toURL()
                            .openConnection(); // 1000 requêtes par jours sans api key avec une moyenne de 500ms de réponse quand j'ai testé
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();
                    String body = sb.toString();
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (json.get("is_vpn").getAsBoolean()
                            || json.get("is_proxy").getAsBoolean()
                            || json.get("is_datacenter").getAsBoolean()
                            || json.get("is_tor").getAsBoolean()
                            || json.get("is_abuser").getAsBoolean())
                        handleVpn(player);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossible de vérifier l'adresse IP: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

}
