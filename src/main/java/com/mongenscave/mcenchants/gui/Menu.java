package com.mongenscave.mcenchants.gui;

import com.mongenscave.mcenchants.data.ItemData;
import com.mongenscave.mcenchants.data.MenuController;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public abstract class Menu implements InventoryHolder {
    public MenuController menuController;
    protected Inventory inventory;
    private static final Map<String, ItemData> itemDataRegistry = new ConcurrentHashMap<>();

    public Menu(@NotNull MenuController menuController) {
        this.menuController = menuController;
    }

    public abstract void handleMenu(final @NotNull InventoryClickEvent event);

    public abstract void setMenuItems();

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract int getMenuTick();

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), MessageProcessor.process(getMenuName()));

        this.setMenuItems();

        menuController.owner().openInventory(inventory);

        int tickRate = getMenuTick();
        if (tickRate > 0) {
            MenuProcessor menuUpdater = new MenuProcessor(this);
            menuUpdater.start(tickRate);
        }
    }

    public void close() {
        MenuProcessor menuUpdater = new MenuProcessor(this);
        menuUpdater.stop();
        MenuController.remove(menuController.owner());
        inventory = null;
    }

    public void updateMenuItems() {
        if (inventory == null) return;

        setMenuItems();
        menuController.owner().updateInventory();
    }

    public static void registerItemData(@NotNull String configPath, @NotNull ItemData itemData) {
        itemDataRegistry.put(configPath, itemData);
    }

    public static ItemData getItemData(@NotNull String configPath) {
        return itemDataRegistry.get(configPath);
    }

    protected void handleItemClick(@NotNull InventoryClickEvent event, @NotNull Player target) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        ItemStack item = clickedItem.clone();
        if (item.getItemMeta() == null) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McProfile.getInstance(), "mcProfile");

        if (!pdc.has(key, PersistentDataType.STRING)) return;

        String configPath = pdc.get(key, PersistentDataType.STRING);
        if (configPath == null) return;

        ItemData itemData = getItemData(configPath);
        if (itemData == null) return;

        Player player = (Player) event.getWhoClicked();

        if (itemData.sound() != null) {
            SoundData sound = itemData.sound();
            player.playSound(player.getLocation(), sound.name(), sound.volume(), sound.pitch());
        }

        if (itemData.commands() != null && !itemData.commands().isEmpty()) {
            for (String command : itemData.commands()) {
                String processedCommand = command.replace("{target}", target.getName());
                player.performCommand(processedCommand);
            }
        }
    }

    public static void clearRegistry() {
        itemDataRegistry.clear();
    }
}