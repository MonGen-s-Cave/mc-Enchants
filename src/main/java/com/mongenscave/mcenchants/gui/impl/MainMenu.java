package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public final class MainMenu extends Menu {

    public MainMenu(@NotNull MenuController menuController) {
        super(menuController);
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot == 0) {
            new EnchanterMenu(MenuController.getMenuUtils(player)).open();
        } else if (slot == 1) {
            new RepairerMenu(MenuController.getMenuUtils(player)).open();
        } else if (slot == 2) {
            new ResolverMenu(MenuController.getMenuUtils(player)).open();
        }
    }

    @Override
    public void setMenuItems() {
        ItemFactory.setItemsForMenu("main-menu.items", inventory);
    }

    @Override
    public String getMenuName() {
        return MenuKey.MENU_MAIN_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKey.MENU_MAIN_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}