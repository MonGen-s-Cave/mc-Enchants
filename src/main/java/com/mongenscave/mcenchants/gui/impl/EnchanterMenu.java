package com.mongenscave.mcenchants.gui.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.gui.Menu;
import com.mongenscave.mcenchants.identifier.key.MenuKey;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.CategoryManager;
import com.mongenscave.mcenchants.model.Category;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class EnchanterMenu extends Menu {
    private final BookManager bookManager;
    private final CategoryManager categoryManager;

    public EnchanterMenu(@NotNull MenuController menuController) {
        super(menuController);
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        this.categoryManager = McEnchants.getInstance().getManagerRegistry().getCategoryManager();
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
        Category category = categoryManager.getCategory(categoryId);

        int requiredXP = category.getPrice();
        int playerXP = getTotalExperience(player);

        if (playerXP < requiredXP) {
            player.sendMessage(MessageKey.NOT_ENOUGH_XP.getMessage());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int newTotalXP = playerXP - requiredXP;
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.giveExp(newTotalXP);

        ItemStack mysteriousBook = bookManager.createMysteriousBook(categoryId);
        player.getInventory().addItem(mysteriousBook);

        player.sendMessage(MessageKey.SUCCESS_PURCHASE.getMessage());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.closeInventory();
    }

    private int getTotalExperience(@NotNull Player player) {
        int level = player.getLevel();
        int totalExp = Math.round(player.getExp() * getExpToLevel(level));

        for (int i = 0; i < level; i++) {
            totalExp += getExpToLevel(i);
        }

        return totalExp;
    }

    private int getExpToLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        else if (level <= 30) return 5 * level - 38;
        else return 9 * level - 158;
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