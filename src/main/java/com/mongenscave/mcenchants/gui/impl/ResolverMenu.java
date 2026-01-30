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

    private final List<ItemStack> placedBookSnapshots = new ArrayList<>();

    public ResolverMenu(@NotNull MenuController menuController) {
        super(menuController);
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.placeableSlots = parsePlaceableSlots();

        SoundUtil.playOpenGuiSound(menuController.owner());
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
        Player player = (Player) event.getWhoClicked();

        int rawSlot = event.getRawSlot();
        int topSize = inventory.getSize();
        int slot = event.getSlot();

        event.setCancelled(true);

        if (ItemKey.RESOLVER_ACCEPT.matchesSlot(slot)) {
            handleItemClick(event, player);
            acceptResolve(player);
            return;
        }

        if (ItemKey.RESOLVER_DENY.matchesSlot(slot)) {
            handleItemClick(event, player);
            denyResolve(player);
            return;
        }

        if (ItemKey.RESOLVER_BACK.matchesSlot(slot)) {
            handleItemClick(event, player);
            close();
            new MainMenu(MenuController.getMenuUtils(player)).open();
            return;
        }

        if (rawSlot >= topSize) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (!bookManager.isRevealedBook(clicked)) return;

            int targetSlot = findFirstFreePlaceableSlot();
            if (targetSlot == -1) return;

            placePreview(clicked, targetSlot);
            SoundUtil.playSuccessSound(player);
        }
    }

    private void placePreview(@NotNull ItemStack source, int slot) {
        ItemStack preview = source.clone();
        preview.setAmount(1);
        inventory.setItem(slot, preview);

        ItemStack snapshot = source.clone();
        snapshot.setAmount(1);
        placedBookSnapshots.add(snapshot);
    }

    private void acceptResolve(@NotNull Player player) {
        if (placedBookSnapshots.isEmpty()) {
            player.sendMessage(MessageKey.RESOLVER_EMPTY_BOOK.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        for (ItemStack snapshot : placedBookSnapshots) {
            if (!hasExactItem(player, snapshot)) {
                SoundUtil.playErrorSound(player);
                return;
            }
        }

        int totalDust = 0;
        String categoryId = null;

        for (ItemStack snapshot : placedBookSnapshots) {
            EnchantedBook bookData = bookManager.getBookData(snapshot);
            if (bookData == null) continue;

            Enchant enchant = enchantManager.getEnchant(bookData.getEnchantId());
            if (enchant == null) continue;

            categoryId = enchant.getCategory().getId();
            totalDust += calculateDustAmount(bookData.getSuccessRate());
        }

        if (categoryId == null || totalDust == 0) {
            SoundUtil.playErrorSound(player);
            return;
        }

        for (ItemStack snapshot : placedBookSnapshots) {
            consumeExactItem(player, snapshot);
        }

        clearPreview();

        ItemStack dust = bookManager.createDust(categoryId);
        dust.setAmount(Math.min(totalDust, 64));
        player.getInventory().addItem(dust);

        player.sendMessage(MessageKey.SUCCESS_RESOLVE.getMessage());
        SoundUtil.playSuccessSound(player);
        player.closeInventory();
    }

    private boolean hasExactItem(@NotNull Player player, @NotNull ItemStack snapshot) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.isSimilar(snapshot)) return true;
        }

        return false;
    }

    private void denyResolve(@NotNull Player player) {
        clearPreview();
        player.sendMessage(MessageKey.SUCCESS_DENY.getMessage());
        SoundUtil.playErrorSound(player);
        player.closeInventory();
    }

    private void clearPreview() {
        for (int slot : placeableSlots) {
            inventory.setItem(slot, null);
        }

        placedBookSnapshots.clear();
    }

    private int findFirstFreePlaceableSlot() {
        for (int slot : placeableSlots) {
            ItemStack current = inventory.getItem(slot);

            if (current == null) return slot;
            if (current.getType() == Material.AIR) return slot;
            if (bookManager.isRevealedBook(current)) continue;

            return slot;
        }

        return -1;
    }

    private boolean consumeExactItem(@NotNull Player player, @NotNull ItemStack snapshot) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (!item.isSimilar(snapshot)) continue;

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItem(i, null);
            }

            return true;
        }

        return false;
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

    @NotNull
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