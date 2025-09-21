package fr.openmc.core.features.economy.commands;

import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.economy.Transaction;
import fr.openmc.core.features.economy.TransactionsManager;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class Pay {

    @Command("pay")
    @Description("Permet de payer un joueur")
    @CommandPermission("omc.commands.pay")
    public void pay(Player player, Player target, @Range(min = 1) double amount) {
        if(player == target) {
            MessagesManager.sendMessage(player, Component.text("§cVous ne pouvez pas vous payer vous-même"), Prefix.OPENMC, MessageType.ERROR, true);
            return;
        }
        if(EconomyManager.withdrawBalance(player.getUniqueId(), amount)) {
            EconomyManager.addBalance(target.getUniqueId(), amount);
            MessagesManager.sendMessage(player, Component.text("§aVous avez payé §e" + target.getName() + "§a de §e" + EconomyManager.getFormattedNumber(amount)), Prefix.OPENMC, MessageType.SUCCESS, true);
            MessagesManager.sendMessage(target, Component.text("§aVous avez reçu §e" + EconomyManager.getFormattedNumber(amount) + "§a de §e" + player.getName()), Prefix.OPENMC, MessageType.INFO, true);

            Bukkit.getScheduler().runTaskAsynchronously(OMCPlugin.getInstance(), () -> {
                TransactionsManager.registerTransaction(new Transaction(
                        target.getUniqueId().toString(),
                        player.getUniqueId().toString(),
                        amount,
                        "Paiement"
                ));
            });
        } else {
            MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas assez d'argent"), Prefix.OPENMC, MessageType.ERROR, true);
        }
    }

}
