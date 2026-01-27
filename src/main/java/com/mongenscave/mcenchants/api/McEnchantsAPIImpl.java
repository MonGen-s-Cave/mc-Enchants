package com.mongenscave.mcenchants.api;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.CategoryManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Category;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantedBook;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class McEnchantsAPIImpl implements McEnchantsAPI {
    private final McEnchants plugin;

    public McEnchantsAPIImpl(@NotNull McEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable Enchant getEnchant(@NotNull String enchantId) {
        return getEnchantManager().getEnchant(enchantId);
    }

    @Override
    public @NotNull Collection<Enchant> getAllEnchants() {
        return getEnchantManager().getEnchants().values();
    }

    @Override
    public @NotNull Collection<Enchant> getEnchantsByCategory(@NotNull String categoryId) {
        return getEnchantManager().getEnchantsByCategory(categoryId);
    }

    @Override
    public @Nullable Enchant getRandomEnchantFromCategory(@NotNull String categoryId) {
        return getEnchantManager().getRandomEnchantFromCategory(categoryId);
    }

    @Override
    public @NotNull Category getCategory(@NotNull String categoryId) {
        return getCategoryManager().getCategory(categoryId);
    }

    @Override
    public @NotNull Map<String, Category> getAllCategories() {
        return getCategoryManager().getCategories();
    }

    @Override
    public @NotNull ItemStack createMysteriousBook(@NotNull String categoryId) {
        return getBookManager().createMysteriousBook(categoryId);
    }

    @Override
    public @NotNull ItemStack createRevealedBook(@NotNull String enchantId, int level, int successRate, int destroyRate) {
        return getBookManager().createRevealedBook(enchantId, level, successRate, destroyRate);
    }

    @Override
    public @NotNull ItemStack createDust(@NotNull String categoryId) {
        return getBookManager().createDust(categoryId);
    }

    @Override
    public @NotNull ItemStack revealBook(@NotNull ItemStack mysteriousBook) {
        return getBookManager().revealBook(mysteriousBook);
    }

    @Override
    public @Nullable EnchantedBook getBookData(@NotNull ItemStack item) {
        return getBookManager().getBookData(item);
    }

    @Override
    public boolean isRevealedBook(@NotNull ItemStack item) {
        return getBookManager().isRevealedBook(item);
    }

    @Override
    public boolean isMysteriousBook(@NotNull ItemStack item) {
        return getBookManager().isMysteriousBook(item);
    }

    @Override
    public boolean isDust(@NotNull ItemStack item) {
        return getBookManager().isDust(item);
    }

    @Override
    public @NotNull Map<String, Integer> getItemEnchants(@NotNull ItemStack item) {
        Map<String, Integer> enchants = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return enchants;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return enchants;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) return enchants;

        String[] entries = enchantsData.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    String enchantId = parts[0].trim();
                    int level = Integer.parseInt(parts[1].trim());
                    enchants.put(enchantId, level);
                } catch (NumberFormatException ignored) {}
            }
        }

        return enchants;
    }

    @Override
    public boolean hasEnchant(@NotNull ItemStack item, @NotNull String enchantId) {
        return getItemEnchants(item).containsKey(enchantId);
    }

    @Override
    public boolean applyEnchant(@NotNull ItemStack item, @NotNull String enchantId, int level) {
        Enchant enchant = getEnchant(enchantId);
        if (enchant == null) return false;
        if (!enchant.canApplyTo(item)) return false;
        if (hasEnchant(item, enchantId)) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "enchants");

        String currentEnchants = pdc.getOrDefault(key, PersistentDataType.STRING, "");
        String newEnchant = enchantId + ":" + level;

        if (currentEnchants.isEmpty()) {
            pdc.set(key, PersistentDataType.STRING, newEnchant);
        } else {
            pdc.set(key, PersistentDataType.STRING, currentEnchants + ";" + newEnchant);
        }

        item.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean removeEnchant(@NotNull ItemStack item, @NotNull String enchantId) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return false;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) return false;

        String[] enchantEntries = enchantsData.split(";");
        StringBuilder newEnchantsData = new StringBuilder();

        boolean found = false;
        for (String entry : enchantEntries) {
            String[] enchantParts = entry.split(":");
            if (enchantParts.length == 2) {
                String currentId = enchantParts[0].trim();

                if (!currentId.equals(enchantId)) {
                    if (!newEnchantsData.isEmpty()) newEnchantsData.append(";");
                    newEnchantsData.append(entry);
                } else {
                    found = true;
                }
            }
        }

        if (!found) return false;

        if (!newEnchantsData.isEmpty()) {
            pdc.set(key, PersistentDataType.STRING, newEnchantsData.toString());
        } else {
            pdc.remove(key);
        }

        item.setItemMeta(meta);
        return true;
    }

    @Override
    public @NotNull EnchantManager getEnchantManager() {
        return plugin.getManagerRegistry().getEnchantManager();
    }

    @Override
    public @NotNull CategoryManager getCategoryManager() {
        return plugin.getManagerRegistry().getCategoryManager();
    }

    @Override
    public @NotNull BookManager getBookManager() {
        return plugin.getManagerRegistry().getBookManager();
    }

    @Override
    public void reload() {
        plugin.getManagerRegistry().reload();
    }
}