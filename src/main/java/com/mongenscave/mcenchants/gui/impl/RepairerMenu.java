package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.model.EnchantedBook;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class RepairerMenu extends Menu {
    private final BookManager bookManager;
    private final int bookInputSlot;
    private final int dustInputSlot;
    private final int outputSlot;

    public RepairerMenu(@NotNull MenuController menuController) {
        super(menuController);
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        this.bookInputSlot = MenuKey.MENU_REPAIRER_BOOK_INPUT.getInt();
        this.dustInputSlot = MenuKey.MENU_REPAIRER_DUST_INPUT.getInt();
        this.outputSlot = MenuKey.MENU_REPAIRER_OUTPUT.getInt();
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == outputSlot) {
            event.setCancelled(false);
            return;
        }

        if (slot == bookInputSlot || slot == dustInputSlot) {
            McEnchants.getInstance().getScheduler().runTaskLater(this::processRepair, 1L);
            return;
        }

        event.setCancelled(true);
    }

    private void processRepair() {
        ItemStack bookItem = inventory.getItem(bookInputSlot);
        ItemStack dustItem = inventory.getItem(dustInputSlot);

        inventory.setItem(outputSlot, null);

        if (bookItem == null || dustItem == null) return;
        if (!bookManager.isRevealedBook(bookItem)) return;
        if (!bookManager.isDust(dustItem)) return;

        String dustCategory = bookManager.getDustCategory(dustItem);
        Integer repairAmount = bookManager.getDustRepairAmount(dustItem);

        if (dustCategory == null || repairAmount == null) return;

        EnchantedBook bookData = bookManager.getBookData(bookItem);
        if (bookData == null) return;

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

        inventory.setItem(bookInputSlot, null);

        if (dustItem.getAmount() > 1) {
            dustItem.setAmount(dustItem.getAmount() - 1);
            inventory.setItem(dustInputSlot, dustItem);
        } else {
            inventory.setItem(dustInputSlot, null);
        }
    }

    @Override
    public void setMenuItems() {
        ItemFactory.setItemsForMenu("repairer-menu.items", inventory);
    }

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