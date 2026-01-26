package com.mongenscave.mcenchants.command;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandEnchant implements OrphanCommand {
    private static final McEnchants plugin = McEnchants.getInstance();

    @CommandPlaceholder
    public void openMainMenu(@NotNull Player player) {
        // menu
    }

    @Subcommand("reload")
    @CommandPermission("mcprofile.reload")
    public void reload(@NotNull CommandSender sender) {
        plugin.getConfiguration().reload();
        plugin.getLanguage().reload();
        plugin.getGuis().reload();
        plugin.getEnchants().reload();
        plugin.getCategory().reload();
        plugin.getApply().reload();
        plugin.getLevels().reload();

        sender.sendMessage(MessageKey.RELOAD.getMessage());
    }
}