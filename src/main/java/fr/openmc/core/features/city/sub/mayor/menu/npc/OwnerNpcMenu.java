package fr.openmc.core.features.city.sub.mayor.menu.npc;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.openmc.api.input.location.ItemInteraction;
import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.api.menulib.utils.ItemUtils;
import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.city.CityPermission;
import fr.openmc.core.features.city.sub.mayor.ElectionType;
import fr.openmc.core.features.city.sub.mayor.managers.NPCManager;
import fr.openmc.core.features.city.sub.mayor.managers.PerkManager;
import fr.openmc.core.features.city.sub.mayor.models.Mayor;
import fr.openmc.core.features.city.sub.mayor.perks.Perks;
import fr.openmc.core.utils.CacheOfflinePlayer;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OwnerNpcMenu extends Menu {

    private final ElectionType electionType;
    private final City city;

    public OwnerNpcMenu(Player owner, City city, ElectionType electionType) {
        super(owner);
        this.city = city;
        this.electionType = electionType;
    }

    @Override
    public @NotNull String getName() {
        return "Menu des Maires - Mandat du Propriétaire";
    }

    @Override
    public String getTexture() {
        return FontImageWrapper.replaceFontImages("§r§f:offset_-38::mayor:");
    }

    @Override
    public @NotNull InventorySize getInventorySize() {
        return InventorySize.LARGEST;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent click) {
        //empty
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Map<Integer, ItemBuilder> getContent() {
        Map<Integer, ItemBuilder> inventory = new HashMap<>();
        Player player = getOwner();

        Mayor mayor = city.getMayor();
        UUID uuidOwner = city.getPlayerWithPermission((CityPermission.OWNER));

        String nameOwner = CacheOfflinePlayer.getOfflinePlayer(city.getPlayerWithPermission((CityPermission.OWNER))).getName();

        if (electionType == ElectionType.ELECTION) {
            Perks perk1 = PerkManager.getPerkById(mayor.getIdPerk1());
            List<Component> loreOwner = new ArrayList<>(List.of(
                    Component.text("§8§oPropriétaire de " + city.getName())
            ));
            loreOwner.add(Component.empty());
            loreOwner.add(Component.text(perk1.getName()));
            loreOwner.addAll(perk1.getLore());

            inventory.put(4, new ItemBuilder(this, ItemUtils.getPlayerSkull(uuidOwner), itemMeta -> {
                itemMeta.displayName(Component.text("§ePropriétaire " + nameOwner));
                itemMeta.lore(loreOwner);
            }));

            ItemStack iaPerk1 = (perk1 != null) ? perk1.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
            String namePerk1 = (perk1 != null) ? perk1.getName() : "§8Réforme Vide";
            List<Component> lorePerk1 = (perk1 != null) ? new ArrayList<>(perk1.getLore()) : null;
            inventory.put(31, new ItemBuilder(this, iaPerk1, itemMeta -> {
                itemMeta.itemName(Component.text(namePerk1));
                itemMeta.lore(lorePerk1);
            }).hide(perk1.getToHide()));
        } else {
            Perks perk1 = PerkManager.getPerkById(mayor.getIdPerk1());
            Perks perk2 = PerkManager.getPerkById(mayor.getIdPerk2());
            Perks perk3 = PerkManager.getPerkById(mayor.getIdPerk3());

            List<Component> loreOwner = new ArrayList<>(List.of(
                    Component.text("§8§oPropriétaire de " + city.getName())
            ));
            loreOwner.add(Component.empty());
            loreOwner.add(Component.text(perk1.getName()));
            loreOwner.addAll(perk1.getLore());
            loreOwner.add(Component.empty());
            loreOwner.add(Component.text(perk2.getName()));
            loreOwner.addAll(perk2.getLore());
            loreOwner.add(Component.empty());
            loreOwner.add(Component.text(perk3.getName()));
            loreOwner.addAll(perk3.getLore());

            inventory.put(4, new ItemBuilder(this, ItemUtils.getPlayerSkull(uuidOwner), itemMeta -> {
                itemMeta.displayName(Component.text("§ePropriétaire " + nameOwner));
                itemMeta.lore(loreOwner);
            }));

            ItemStack iaPerk1 = (perk1 != null) ? perk1.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
            String namePerk1 = (perk1 != null) ? perk1.getName() : "§8Réforme Vide";
            List<Component> lorePerk1 = (perk1 != null) ? new ArrayList<>(perk1.getLore()) : null;
            inventory.put(20, new ItemBuilder(this, iaPerk1, itemMeta -> {
                itemMeta.itemName(Component.text(namePerk1));
                itemMeta.lore(lorePerk1);
            }).hide(perk1.getToHide()));

            ItemStack iaPerk2 = (perk2 != null) ? perk2.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
            String namePerk2 = (perk2 != null) ? perk2.getName() : "§8Réforme Vide";
            List<Component> lorePerk2 = (perk2 != null) ? new ArrayList<>(perk2.getLore()) : null;
            inventory.put(22, new ItemBuilder(this, iaPerk2, itemMeta -> {
                itemMeta.itemName(Component.text(namePerk2));
                itemMeta.lore(lorePerk2);
            }).hide(perk2.getToHide()));

            ItemStack iaPerk3 = (perk3 != null) ? perk3.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
            String namePerk3 = (perk3 != null) ? perk3.getName() : "§8Réforme Vide";
            List<Component> lorePerk3 = (perk3 != null) ? new ArrayList<>(perk3.getLore()) : null;
            inventory.put(24, new ItemBuilder(this, iaPerk3, itemMeta -> {
                itemMeta.customName(Component.text(namePerk3));
                itemMeta.lore(lorePerk3);
            }).hide(perk3.getToHide()));
        }

        if (mayor.getMayorUUID().equals(player.getUniqueId())) {
            inventory.put(46, new ItemBuilder(this, Material.ENDER_PEARL, itemMeta -> {
                itemMeta.itemName(Component.text("§aDéplacer ce NPC"));
                itemMeta.lore(List.of(
                        Component.text("§7Vous allez pouvoir déplacer ce NPC"),
                        Component.text("§e§lCLIQUEZ ICI POUR CONTINUER")
                ));
            }).setOnClick(inventoryClickEvent -> {
                List<Component> loreItemNPC = List.of(
                        Component.text("§7Cliquez sur l'endroit où vous voulez déplacer le §9NPC")
                );
                ItemStack itemToGive = new ItemStack(Material.STICK);
                ItemMeta itemMeta = itemToGive.getItemMeta();

                itemMeta.displayName(Component.text("§7Emplacement du §9NPC"));
                itemMeta.lore(loreItemNPC);
                itemToGive.setItemMeta(itemMeta);
                ItemInteraction.runLocationInteraction(
                        player,
                        itemToGive,
                        "mayor:owner-npc-move",
                        300,
                        "§7Vous avez 300s pour séléctionner votre emplacement",
                        "§7Vous n'avez pas eu le temps de déplacer votre NPC",
                        locationClick -> {
                            if (locationClick == null) return true;

                            Chunk chunk = locationClick.getChunk();

                            City cityByChunk = CityManager.getCityFromChunk(chunk.getX(), chunk.getZ());
                            if (cityByChunk == null) {
                                MessagesManager.sendMessage(player, Component.text("§cImpossible de mettre le NPC en dehors de votre ville"), Prefix.CITY, MessageType.ERROR, false);
                                return false;
                            }

                            City playerCity = CityManager.getPlayerCity(player.getUniqueId());

                            if (playerCity == null) {
                                return false;
                            }

                            if (!cityByChunk.getUniqueId().equals(playerCity.getUniqueId())) {
                                MessagesManager.sendMessage(player, Component.text("§cImpossible de mettre le NPC en dehors de votre ville"), Prefix.CITY, MessageType.ERROR, false);
                                return false;
                            }

                            NPCManager.moveNPC("owner", locationClick, city.getUniqueId());
                            NPCManager.updateNPCS(city.getUniqueId());
                            return true;
                        },
                        null
                );
            }));
        }
        return inventory;
    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }
}