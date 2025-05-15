package fr.openmc.anothermenulib.events;

import fr.openmc.anothermenulib.menu.ClickType;
import org.bukkit.entity.Player;

public record InventoryClickEvent(ClickType clickType, int slot, Player player) {
}
