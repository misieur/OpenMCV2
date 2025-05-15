package fr.openmc.core.features.mainmenu;

import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.mainmenu.listeners.PacketListener;
import org.bukkit.entity.Player;

public class MainMenu {

    private final OMCPlugin plugin;

    public MainMenu(OMCPlugin plugin) {
        this.plugin = plugin;
        new PacketListener(plugin);
    }

    public static void openMainMenu(Player player) {

    }
}
