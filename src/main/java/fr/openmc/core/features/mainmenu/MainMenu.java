package fr.openmc.core.features.mainmenu;

import fr.openmc.api.anothermenulib.AnotherMenuLib;
import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.mainmenu.listeners.PacketListener;
import lombok.Getter;
import org.bukkit.entity.Player;

public class MainMenu {

    private final OMCPlugin plugin;
    @Getter
    private static final fr.openmc.core.features.mainmenu.menus.MainMenu menu = new fr.openmc.core.features.mainmenu.menus.MainMenu();

    public MainMenu(OMCPlugin plugin) {
        this.plugin = plugin;
        new PacketListener(plugin);
    }

    public static void openMainMenu(Player player) {
        AnotherMenuLib.openMenu(menu, player);
    }
}
