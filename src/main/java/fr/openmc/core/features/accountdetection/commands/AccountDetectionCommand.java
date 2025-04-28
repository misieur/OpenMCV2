package fr.openmc.core.features.accountdetection.commands;

import fr.openmc.core.features.accountdetection.AccountDetectionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.IOException;

@Command({"accountDetection"})
public class AccountDetectionCommand {
    @DefaultFor("~")
    void mainCommand(CommandSender sender) {
        sender.sendMessage("§cVeuillez spécifier une commande valide.");
    }
    @CommandPermission("op")
    @Subcommand("reload")
    void reloadCommand(CommandSender sender) {
        AccountDetectionManager.getInstance().reload();
        sender.sendMessage("§aLa configuration a été rechargée.");
    }
    @CommandPermission("omc.modo")
    @Subcommand("exemptePlayer")
    void exemptPlayerCommand(CommandSender sender, Player player) {
        try {
            AccountDetectionManager.getInstance().addExemptedPlayer(player.getUniqueId());
            sender.sendMessage("§aLe joueur " + player.getName() + " a été exempté.");
        } catch (IOException e) {
            sender.sendMessage("§cErreur lors de l'ajout du joueur à la liste des exemptés: " + e.getMessage());
        }

    }
}
