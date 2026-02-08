package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.ItemKey;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.CategoryManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantedBook;
import com.mongenscave.mcenchants.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class RepairerMenu extends Menu {
    private final BookManager bookManager;
    private final EnchantManager enchantManager;
    private final int bookInputSlot;
    private final int dustInputSlot;
    private final int outputSlot;
    private ItemStack placedBookItem;
    private ItemStack placedDustItem;

    public RepairerMenu(@NotNull MenuController menuController) {
        super(menuController);
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.bookInputSlot = MenuKey.MENU_REPAIRER_BOOK_INPUT.getInt();
        this.dustInputSlot = MenuKey.MENU_REPAIRER_DUST_INPUT.getInt();
        this.outputSlot = MenuKey.MENU_REPAIRER_OUTPUT.getInt();
        SoundUtil.playOpenGuiSound(menuController.owner());
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        int topSize = inventory.getSize();
        int slot = event.getSlot();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (rawSlot >= topSize) {
            if (bookManager.isRevealedBook(clicked)) {
                placeItem(clicked, bookInputSlot);
                processRepair(player);
                SoundUtil.playSuccessSound(player);
            } else if (bookManager.isDust(clicked)) {
                placeItem(clicked, dustInputSlot);
                processRepair(player);
                SoundUtil.playSuccessSound(player);
            }
            return;
        }

        if (rawSlot == outputSlot) {
            handleOutputTake(player);
            return;
        }

        if (ItemKey.REPAIRER_BACK.matchesSlot(event.getSlot())) {
            handleItemClick(event, player);
            close();
            new MainMenu(MenuController.getMenuUtils(player)).open();
        }
    }

    private void handleOutputTake(@NotNull Player player) {
        ItemStack result = inventory.getItem(outputSlot);
        if (result == null || result.getType() == Material.AIR) return;

        if (placedBookItem == null || placedDustItem == null) {
            SoundUtil.playErrorSound(player);
            return;
        }

        if (!consumeItem(player, placedBookItem) || !consumeItem(player, placedDustItem)) {
            SoundUtil.playErrorSound(player);
            return;
        }

        player.getInventory().addItem(result);

        inventory.setItem(outputSlot, null);
        inventory.setItem(bookInputSlot, null);
        inventory.setItem(dustInputSlot, null);

        placedBookItem = null;
        placedDustItem = null;

        SoundUtil.playSuccessSound(player);

        setMenuItems();
    }

    private boolean consumeItem(@NotNull Player player, @NotNull ItemStack snapshot) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            if (!item.isSimilar(snapshot)) continue;

            if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
            else player.getInventory().setItem(i, null);

            return true;
        }
        return false;
    }

    private void placeItem(@NotNull ItemStack source, int slot) {
        ItemStack preview = source.clone();
        preview.setAmount(1);
        inventory.setItem(slot, preview);

        ItemStack item = source.clone();
        item.setAmount(1);

        if (slot == bookInputSlot) this.placedBookItem = item;
        else if (slot == dustInputSlot) this.placedDustItem = item;
    }

    private void processRepair(@NotNull Player player) {
        ItemStack bookItem = inventory.getItem(bookInputSlot);
        ItemStack dustItem = inventory.getItem(dustInputSlot);

        inventory.setItem(outputSlot, null);

        if (bookItem == null || dustItem == null) return;
        if (!bookManager.isRevealedBook(bookItem)) return;
        if (!bookManager.isDust(dustItem)) return;

        Integer repairAmount = bookManager.getDustRepairAmount(dustItem);
        if (repairAmount == null) return;

        EnchantedBook bookData = bookManager.getBookData(bookItem);
        if (bookData == null) return;

        Enchant enchant = enchantManager.getEnchant(bookData.getEnchantId());
        if (enchant == null) return;

        String bookCategory = enchant.getCategory().getId();
        String dustCategory = bookManager.getDustCategory(dustItem);

        if (!bookCategory.equals(dustCategory)) {
            player.sendMessage(MessageKey.CATEGORY_MISMATCH.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        bookData.repair(repairAmount);

        if (!bookData.isValid()) {
            bookData.setSuccessRate(100);
            bookData.setDestroyRate(0);
        }

        ItemStack repairedBook = bookManager.createRevealedBook(
                bookData.getEnchantId(),
                bookData.getLevel(),
                bookData.getSuccessRate(),
                bookData.getDestroyRate()
        );

        inventory.setItem(outputSlot, repairedBook);
    }

    @Override
    public void setMenuItems() {
        ItemFactory.setItemsForMenu("repairer-menu.items", inventory);
    }

    @NotNull
    @Override
    public String getMenuName() {
        return MenuKey.MENU_REPAIRER_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKey.MENU_REPAIRER_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}