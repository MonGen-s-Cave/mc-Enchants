package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class EnchanterMenu extends Menu {
    private final BookManager bookManager;

    public EnchanterMenu(@NotNull MenuController menuController) {
        super(menuController);
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey categoryKey = new NamespacedKey(McEnchants.getInstance(), "category");

        if (!pdc.has(categoryKey, PersistentDataType.STRING)) return;

        String categoryId = pdc.get(categoryKey, PersistentDataType.STRING);
        if (categoryId == null) return;

        Player player = (Player) event.getWhoClicked();

        // TODO XP check

        ItemStack mysteriousBook = bookManager.createMysteriousBook(categoryId);
        player.getInventory().addItem(mysteriousBook);

        player.sendMessage(MessageProcessor.process("&aSimerült vásárolni egy mysteryous könyvet!"));
        player.closeInventory();
    }

    @Override
    public void setMenuItems() {
        ItemFactory.setItemsForMenu("enchanter-menu.items", inventory);
    }

    @Override
    public String getMenuName() {
        return MenuKey.MENU_ENCHANTER_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return MenuKey.MENU_ENCHANTER_SIZE.getInt();
    }

    @Override
    public int getMenuTick() {
        return 0;
    }
}