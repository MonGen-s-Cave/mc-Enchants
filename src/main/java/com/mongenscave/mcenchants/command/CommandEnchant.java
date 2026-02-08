package com.mongenscave.mcenchants.command;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.annotation.Category;
import com.mongenscave.mcenchants.annotation.Enchant;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.impl.MainMenu;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.concurrent.ThreadLocalRandom;

public class CommandEnchant implements OrphanCommand {
    private static final McEnchants plugin = McEnchants.getInstance();
    private static final BookManager bookManager = plugin.getManagerRegistry().getBookManager();
    private static final EnchantManager enchantManager = plugin.getManagerRegistry().getEnchantManager();

    @CommandPlaceholder
    public void openMainMenu(@NotNull Player player) {
        MenuController controller = MenuController.getMenuUtils(player);
        new MainMenu(controller).open();
    }

    @Subcommand("reload")
    @CommandPermission("mcenchants.reload")
    public void reload(@NotNull CommandSender sender) {
        plugin.getConfiguration().reload();
        plugin.getLanguage().reload();
        plugin.getGuis().reload();
        plugin.getEnchants().reload();
        plugin.getCategory().reload();
        plugin.getApply().reload();
        plugin.getLevels().reload();
        plugin.getManagerRegistry().reload();

        sender.sendMessage(MessageKey.RELOAD.getMessage());
    }

    @Subcommand("givebook")
    @CommandPermission("mcenchants.admin.givebook")
    public void giveBook(@NotNull CommandSender sender, @NotNull Player target, @Category @NotNull String categoryId, @Default("1") @Range(from = 1, to = 64) int amount) {
        ItemStack mysteriousBook = bookManager.createMysteriousBook(categoryId);
        mysteriousBook.setAmount(amount);

        target.getInventory().addItem(mysteriousBook);

        sender.sendMessage(MessageKey.GIVEBOOK_SENDER.getMessage());
    }

    @Subcommand("giveenchant")
    @CommandPermission("mcenchants.admin.giveenchant")
    public void giveEnchant(
            @NotNull CommandSender sender,
            @NotNull Player target,
            @Enchant @NotNull String enchantId,
            @Default("1") int level,
            @Optional Integer successRate,
            @Default("1") @Range(from = 1, to = 64) int amount) {
        com.mongenscave.mcenchants.model.Enchant enchant = enchantManager.getEnchant(enchantId);

        if (enchant == null) {
            sender.sendMessage("No enchant like this SHEESH");
            return;
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            sender.sendMessage("Wrong level!");
            return;
        }

        int finalSuccessRate;
        int destroyRate;

        if (successRate == null) {
            finalSuccessRate = ThreadLocalRandom.current().nextInt(50, 81);
            destroyRate = 100 - finalSuccessRate;
        } else {
            if (successRate == 100) {
                finalSuccessRate = 100;
                destroyRate = 0;
            } else {
                finalSuccessRate = Math.max(0, Math.min(100, successRate));
                destroyRate = 100 - finalSuccessRate;
            }
        }

        ItemStack revealedBook = bookManager.createRevealedBook(enchantId, level, finalSuccessRate, destroyRate);

        revealedBook.setAmount(amount);
        target.getInventory().addItem(revealedBook);
        sender.sendMessage(MessageKey.GIVEENCHANT_SENDER.getMessage());
    }
}