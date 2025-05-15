package fr.openmc.api.anothermenulib.events;

import fr.openmc.api.anothermenulib.menu.ClickType;
import org.bukkit.entity.Player;

public record InventoryClickEvent(ClickType clickType, int slot, Player player) {
}
