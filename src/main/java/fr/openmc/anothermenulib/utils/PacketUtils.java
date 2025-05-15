package fr.openmc.anothermenulib.utils;

import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Stream;

public class PacketUtils {
    public static void sendOpenInventoryPacket(Player player, int containerId, MenuType<?> menuType, net.kyori.adventure.text.Component title) {
        Component component = Component.Serializer.fromJson(
                JSONComponentSerializer.json().serialize(title),
                HolderLookup.Provider.create(Stream.empty())
        );
        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(containerId, menuType, component != null ? component : Component.empty());
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void sendContainerContentPacket(Player player, int containerId, int stateId, List<ItemStack> items, ItemStack cursorItem) {
        net.minecraft.world.item.ItemStack nmsCursorItem = net.minecraft.world.item.ItemStack.fromBukkitCopy(cursorItem);
        List<net.minecraft.world.item.ItemStack> nmsItems = items.stream().map(net.minecraft.world.item.ItemStack::fromBukkitCopy).toList();
        ClientboundContainerSetContentPacket packet = new ClientboundContainerSetContentPacket(containerId,stateId, nmsItems, nmsCursorItem);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void sendCloseInventoryPacket(Player player, int containerId) {
        ClientboundContainerClosePacket packet = new ClientboundContainerClosePacket(containerId);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
}
