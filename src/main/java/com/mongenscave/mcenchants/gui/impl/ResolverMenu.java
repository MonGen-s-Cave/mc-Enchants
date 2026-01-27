package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.ItemKey;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantedBook;
import com.mongenscave.mcenchants.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ResolverMenu extends Menu {
    private final BookManager bookManager;
    private final EnchantManager enchantManager;
    private final List<Integer> placeableSlots;

    public ResolverMenu(@NotNull MenuController menuController) {
        super(menuController);
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.placeableSlots = parsePlaceableSlots();
    }

    @NotNull
    private List<Integer> parsePlaceableSlots() {
        List<Integer> slots = new ArrayList<>();
        String slotsStr = MenuKey.MENU_RESOLVER_PLACEABLE_SLOTS.getString();

        if (slotsStr.isEmpty()) return slots;

        String[] parts = slotsStr.split(",");
        for (String part : parts) {
            try {
                slots.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {}
        }

        return slots;
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        if (ItemKey.RESOLVER_ACCEPT.matchesSlot(slot)) {
            event.setCancelled(true);
            handleItemClick(event, player);
            acceptResolve(player);
            return;
        }

        if (ItemKey.RESOLVER_DENY.matchesSlot(slot)) {
            event.setCancelled(true);
            handleItemClick(event, player);
            denyResolve(player);
            return;
        }

        if (!placeableSlots.contains(slot)) event.setCancelled(true);
    }

    private void acceptResolve(@NotNull Player player) {
        List<ItemStack> books = new ArrayList<>();

        for (int slot : placeableSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && bookManager.isRevealedBook(item)) {
                books.add(item);
            }
        }

        if (books.isEmpty()) {
            player.sendMessage(MessageKey.RESOLVER_EMPTY_BOOK.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        int totalDust = 0;
        String categoryId = null;

        for (ItemStack book : books) {
            EnchantedBook bookData = bookManager.getBookData(book);
            if (bookData == null) continue;

            Enchant enchant = enchantManager.getEnchant(bookData.getEnchantId());
            if (enchant == null) continue;

            categoryId = enchant.getCategory().getId();

            int successRate = bookData.getSuccessRate();
            int dustAmount = calculateDustAmount(successRate);
            totalDust += dustAmount * book.getAmount();
        }

        if (categoryId == null || totalDust == 0) {
            player.sendMessage(MessageKey.ERROR_DUE_RESOLVER.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        for (int slot : placeableSlots) {
            inventory.setItem(slot, null);
        }

        ItemStack dust = bookManager.createDust(categoryId);
        dust.setAmount(Math.min(totalDust, 64));

        player.getInventory().addItem(dust);
        player.sendMessage(MessageKey.SUCCESS_RESOLVE.getMessage());
        SoundUtil.playSuccessSound(player);
        player.closeInventory();
    }

    private void denyResolve(@NotNull Player player) {
        for (int slot : placeableSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                player.getInventory().addItem(item);
                inventory.setItem(slot, null);
            }
        }

        player.sendMessage(MessageKey.SUCCESS_DENY.getMessage());
        SoundUtil.playErrorSound(player);
        player.closeInventory();
    }

    private int calculateDustAmount(int successRate) {
        if (successRate >= 90) return ThreadLocalRandom.current().nextInt(8, 15);
        if (successRate >= 75) return ThreadLocalRandom.current().nextInt(6, 12);
        if (successRate >= 60) return ThreadLocalRandom.current().nextInt(4, 9);
        if (successRate >= 40) return ThreadLocalRandom.current().nextInt(2, 6);
        return ThreadLocalRandom.current().nextInt(1, 4);
    }

    @Override
    public void setMenuItems() {
        ItemFactory.setItemsForMenu("resolver-menu.items", inventory);
    }

    @Override
    public String getMenuName() {
        return MenuKey.MENU_RESOLVER_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKey.MENU_RESOLVER_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}