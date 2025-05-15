package fr.openmc.anothermenulib.menu;

import fr.openmc.anothermenulib.events.InventoryClickEvent;
import fr.openmc.anothermenulib.events.InventoryCloseEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface Menu {
    Component getTitle();
    InventoryType getInventoryType();
    Map<Integer, ItemStack> getContent();
    void onInventoryClick(InventoryClickEvent inventoryClickEvent);
    void onInventoryClose(InventoryCloseEvent inventoryCloseEvent);
    boolean isCursorItemEnabled();
}
