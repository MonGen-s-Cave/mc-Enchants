package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.model.EnchantedBook;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (slot == outputSlot) {
            if (current != null && current.getType() != Material.AIR) {
                event.setCancelled(false);
                McEnchants.getInstance().getScheduler().runTaskLater(() -> {
                    if (inventory.getItem(outputSlot) == null || inventory.getItem(outputSlot).getType() == Material.AIR) {
                        inventory.setItem(bookInputSlot, null);
                        inventory.setItem(dustInputSlot, null);
                    }
                }, 1L);
            }
            return;
        }

        if (slot == bookInputSlot) {
            if (cursor.getType() != Material.AIR) {
                if (bookManager.isRevealedBook(cursor)) {
                    if (current != null && current.getType() != Material.AIR) {
                        if (bookManager.isRevealedBook(current)) {
                            event.setCancelled(false);
                            McEnchants.getInstance().getScheduler().runTaskLater(this::processRepair, 2L);
                        } else {
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(false);
                        McEnchants.getInstance().getScheduler().runTaskLater(this::processRepair, 2L);
                    }
                } else {
                    event.setCancelled(true);
                    player.sendMessage(MessageProcessor.process("&cCsak revealed könyvet rakhatsz ide!"));
                }
            } else {
                event.setCancelled(false);
                McEnchants.getInstance().getScheduler().runTaskLater(() -> inventory.setItem(outputSlot, null), 1L);
            }
            return;
        }

        if (slot == dustInputSlot) {
            if (cursor.getType() != Material.AIR) {
                if (bookManager.isDust(cursor)) {
                    if (current != null && current.getType() != Material.AIR) {
                        if (bookManager.isDust(current)) {
                            event.setCancelled(false);
                            McEnchants.getInstance().getScheduler().runTaskLater(this::processRepair, 2L);
                        } else {
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(false);
                        McEnchants.getInstance().getScheduler().runTaskLater(this::processRepair, 2L);
                    }
                } else {
                    event.setCancelled(true);
                    player.sendMessage(MessageProcessor.process("&cCsak port rakhatsz ide!"));
                }
            } else {
                event.setCancelled(false);
                McEnchants.getInstance().getScheduler().runTaskLater(() -> inventory.setItem(outputSlot, null), 1L);
            }
            return;
        }

        event.setCancelled(true);
    }

    private void processRepair() {
        ItemStack bookItem = inventory.getItem(bookInputSlot);
        ItemStack dustItem = inventory.getItem(dustInputSlot);

        inventory.setItem(outputSlot, null);

        if (bookItem == null || dustItem == null) return;
        if (bookItem.getType() == Material.AIR || dustItem.getType() == Material.AIR) return;
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