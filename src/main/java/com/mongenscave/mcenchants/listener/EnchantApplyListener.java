package com.mongenscave.mcenchants.listener;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantedBook;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import com.mongenscave.mcenchants.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("deprecation")
public final class EnchantApplyListener implements Listener {
    private final BookManager bookManager;
    private final EnchantManager enchantManager;

    public EnchantApplyListener() {
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) return;

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;
        if (cursor.getType() == Material.AIR || clicked.getType() == Material.AIR) return;
        if (!bookManager.isRevealedBook(cursor)) return;

        event.setCancelled(true);

        EnchantedBook bookData = bookManager.getBookData(cursor);
        if (bookData == null) {
            player.sendMessage(MessageKey.WRONG_BOOK.getMessage());
            return;
        }

        Enchant enchant = enchantManager.getEnchant(bookData.getEnchantId());
        if (enchant == null) {
            player.sendMessage(MessageKey.WRONG_ENCHANT.getMessage());
            return;
        }

        if (!enchant.canApplyTo(clicked)) {
            player.sendMessage(MessageKey.NOT_APPLIABLE.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        if (hasEnchant(clicked, enchant.getId())) {
            player.sendMessage(MessageKey.ALREADY_HAS.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        int maxEnchantsPerItem = McEnchants.getInstance().getConfiguration().getInt("enchant-per-item", 5);
        int currentEnchantCount = getEnchantCount(clicked);

        if (currentEnchantCount >= maxEnchantsPerItem) {
            player.sendMessage(MessageKey.MAX_ENCHANTS_REACHED.getMessage());
            SoundUtil.playErrorSound(player);
            return;
        }

        applyEnchant(player, clicked, enchant, bookData, event);
    }

    private void applyEnchant(@NotNull Player player, @NotNull ItemStack item, @NotNull Enchant enchant, @NotNull EnchantedBook bookData, @NotNull InventoryClickEvent event) {
        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        boolean success = roll <= bookData.getSuccessRate();

        if (success) {
            addEnchantToItem(item, enchant.getId(), bookData.getLevel());
            player.sendMessage(MessageKey.SUCCESS_APPLY.getMessage());
            SoundUtil.playSuccessSound(player);
            player.setItemOnCursor(null);
        } else {
            event.setCurrentItem(null);
            player.sendMessage(MessageKey.APPLY_FAIL.getMessage());
            SoundUtil.playErrorSound(player);
            player.setItemOnCursor(null);
        }
    }

    private boolean hasEnchant(@NotNull ItemStack item, @NotNull String enchantId) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return false;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null) return false;

        return enchantsData.contains(enchantId + ":");
    }

    private int getEnchantCount(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return 0;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) return 0;

        String[] entries = enchantsData.split(";");
        return entries.length;
    }

    private void addEnchantToItem(@NotNull ItemStack item, @NotNull String enchantId, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        String currentEnchants = pdc.getOrDefault(key, PersistentDataType.STRING, "");
        String newEnchant = enchantId + ":" + level;

        if (currentEnchants.isEmpty()) pdc.set(key, PersistentDataType.STRING, newEnchant);
        else pdc.set(key, PersistentDataType.STRING, currentEnchants + ";" + newEnchant);

        Enchant enchant = enchantManager.getEnchant(enchantId);
        if (enchant != null && enchant.isItemLoreEnabled()) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

            String romanLevel = getRomanNumeral(level);
            for (String line : enchant.getItemLoreLines()) {
                String processedLine = MessageProcessor.process(line
                        .replace("{categoryColor}", enchant.getCategory().getColor())
                        .replace("{level}", romanLevel));
                lore.add(processedLine);
            }

            meta.setLore(lore);
        }

        item.setItemMeta(meta);
    }

    @NotNull
    private String getRomanNumeral(int number) {
        List<String> levels = McEnchants.getInstance().getLevels().getList("levels");

        for (String level : levels) {
            String[] parts = level.split(":");
            if (parts.length == 2) {
                try {
                    if (Integer.parseInt(parts[0]) == number) {
                        return parts[1];
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return String.valueOf(number);
    }
}