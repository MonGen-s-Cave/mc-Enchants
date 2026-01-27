package com.mongenscave.mcenchants.api;

import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.CategoryManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Category;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantedBook;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface McEnchantsAPI {

    /**
     * Gets an enchant by its ID
     *
     * @param enchantId The ID of the enchant
     * @return The enchant, or null if not found
     */
    @Nullable
    Enchant getEnchant(@NotNull String enchantId);

    /**
     * Gets all registered enchants
     *
     * @return Collection of all enchants
     */
    @NotNull
    Collection<Enchant> getAllEnchants();

    /**
     * Gets all enchants in a specific category
     *
     * @param categoryId The category ID
     * @return Collection of enchants in the category
     */
    @NotNull
    Collection<Enchant> getEnchantsByCategory(@NotNull String categoryId);

    /**
     * Gets a random enchant from a specific category
     *
     * @param categoryId The category ID
     * @return A random enchant, or null if category is empty
     */
    @Nullable
    Enchant getRandomEnchantFromCategory(@NotNull String categoryId);

    /**
     * Gets a category by its ID
     *
     * @param categoryId The category ID
     * @return The category
     */
    @NotNull
    Category getCategory(@NotNull String categoryId);

    /**
     * Gets all registered categories
     *
     * @return Map of category ID to Category
     */
    @NotNull
    Map<String, Category> getAllCategories();

    /**
     * Creates a mysterious book for a specific category
     *
     * @param categoryId The category ID
     * @return The mysterious book ItemStack
     */
    @NotNull
    ItemStack createMysteriousBook(@NotNull String categoryId);

    /**
     * Creates a revealed enchant book
     *
     * @param enchantId The enchant ID
     * @param level The enchant level
     * @param successRate The success rate (0-100)
     * @param destroyRate The destroy rate (0-100)
     * @return The revealed book ItemStack
     */
    @NotNull
    ItemStack createRevealedBook(@NotNull String enchantId, int level, int successRate, int destroyRate);

    /**
     * Creates dust for a specific category
     *
     * @param categoryId The category ID
     * @return The dust ItemStack
     */
    @NotNull
    ItemStack createDust(@NotNull String categoryId);

    /**
     * Reveals a mysterious book
     *
     * @param mysteriousBook The mysterious book to reveal
     * @return The revealed book
     */
    @NotNull
    ItemStack revealBook(@NotNull ItemStack mysteriousBook);

    /**
     * Gets enchant book data from an item
     *
     * @param item The item to check
     * @return The book data, or null if not an enchant book
     */
    @Nullable
    EnchantedBook getBookData(@NotNull ItemStack item);

    /**
     * Checks if an item is a revealed enchant book
     *
     * @param item The item to check
     * @return true if revealed book
     */
    boolean isRevealedBook(@NotNull ItemStack item);

    /**
     * Checks if an item is a mysterious book
     *
     * @param item The item to check
     * @return true if mysterious book
     */
    boolean isMysteriousBook(@NotNull ItemStack item);

    /**
     * Checks if an item is dust
     *
     * @param item The item to check
     * @return true if dust
     */
    boolean isDust(@NotNull ItemStack item);

    /**
     * Gets all enchants applied to an item
     *
     * @param item The item to check
     * @return Map of enchant ID to level
     */
    @NotNull
    Map<String, Integer> getItemEnchants(@NotNull ItemStack item);

    /**
     * Checks if an item has a specific enchant
     *
     * @param item The item to check
     * @param enchantId The enchant ID
     * @return true if item has the enchant
     */
    boolean hasEnchant(@NotNull ItemStack item, @NotNull String enchantId);

    /**
     * Applies an enchant to an item
     *
     * @param item The item to enchant
     * @param enchantId The enchant ID
     * @param level The enchant level
     * @return true if successful
     */
    boolean applyEnchant(@NotNull ItemStack item, @NotNull String enchantId, int level);

    /**
     * Removes an enchant from an item
     *
     * @param item The item
     * @param enchantId The enchant ID to remove
     * @return true if successful
     */
    boolean removeEnchant(@NotNull ItemStack item, @NotNull String enchantId);

    /**
     * Gets the EnchantManager
     *
     * @return The EnchantManager instance
     */
    @NotNull
    EnchantManager getEnchantManager();

    /**
     * Gets the CategoryManager
     *
     * @return The CategoryManager instance
     */
    @NotNull
    CategoryManager getCategoryManager();

    /**
     * Gets the BookManager
     *
     * @return The BookManager instance
     */
    @NotNull
    BookManager getBookManager();

    /**
     * Reloads all plugin configurations and managers
     */
    void reload();
}