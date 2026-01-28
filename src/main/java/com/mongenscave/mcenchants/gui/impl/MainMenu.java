package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.ItemKey;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.util.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public final class MainMenu extends Menu {

    public MainMenu(@NotNull MenuController menuController) {
        super(menuController);
        SoundUtil.playOpenGuiSound(menuController.owner());
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        if (ItemKey.MAIN_ENCHANTER.matchesSlot(slot)) {
            handleItemClick(event, player);
            new EnchanterMenu(MenuController.getMenuUtils(player)).open();
            return;
        }

        if (ItemKey.MAIN_REPAIRER.matchesSlot(slot)) {
            handleItemClick(event, player);
            new RepairerMenu(MenuController.getMenuUtils(player)).open();
            return;
        }

        if (ItemKey.MAIN_RESOLVER.matchesSlot(slot)) {
            handleItemClick(event, player);
            new ResolverMenu(MenuController.getMenuUtils(player)).open();
            return;
        }

        if (ItemKey.MAIN_CLOSE.matchesSlot(slot)) {
            handleItemClick(event, player);
            player.closeInventory();
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