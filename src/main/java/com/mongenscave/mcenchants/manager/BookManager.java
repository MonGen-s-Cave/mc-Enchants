package com.mongenscave.mcenchants.manager;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.model.Category;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantedBook;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BookManager {
    private static final Random RANDOM = new Random();
    private static final NamespacedKey ENCHANT_KEY = new NamespacedKey(McEnchants.getInstance(), "enchant_id");
    private static final NamespacedKey LEVEL_KEY = new NamespacedKey(McEnchants.getInstance(), "enchant_level");
    private static final NamespacedKey SUCCESS_KEY = new NamespacedKey(McEnchants.getInstance(), "success_rate");
    private static final NamespacedKey DESTROY_KEY = new NamespacedKey(McEnchants.getInstance(), "destroy_rate");
    private static final NamespacedKey REVEALED_KEY = new NamespacedKey(McEnchants.getInstance(), "revealed");

    private final EnchantManager enchantManager;
    private final CategoryManager categoryManager;

    public BookManager(@NotNull EnchantManager enchantManager, @NotNull CategoryManager categoryManager) {
        this.enchantManager = enchantManager;
        this.categoryManager = categoryManager;
    }

    @NotNull
    public ItemStack createMysteriousBook(@NotNull String categoryId) {
        Category category = categoryManager.getCategory(categoryId);
        Config config = McEnchants.getInstance().getConfiguration();

        String material = config.getString("mysterious-book-item.material", "BOOK");
        ItemStack book = new ItemStack(Material.valueOf(material.toUpperCase()));

        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;

        String name = config.getString("mysterious-book-item.name", "{categoryColor}{categoryName} &fKönyv")
                .replace("{categoryColor}", category.getColor())
                .replace("{categoryName}", category.getName());

        meta.setDisplayName(MessageProcessor.process(name));

        List<String> lore = config.getStringList("mysterious-book-item.lore").stream()
                .map(MessageProcessor::process)
                .collect(Collectors.toList());

        meta.setLore(lore);

        int modelData = config.getInt("mysterious-book-item.modeldata", 0);
        if (modelData > 0) meta.setCustomModelData(modelData);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(McEnchants.getInstance(), "mysterious_category"),
                PersistentDataType.STRING, categoryId);

        book.setItemMeta(meta);
        return book;
    }

    @NotNull
    public ItemStack revealBook(@NotNull ItemStack mysteriousBook) {
        ItemMeta meta = mysteriousBook.getItemMeta();
        if (meta == null) return mysteriousBook;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String categoryId = pdc.get(new NamespacedKey(McEnchants.getInstance(), "mysterious_category"),
                PersistentDataType.STRING);

        if (categoryId == null) return mysteriousBook;

        Enchant randomEnchant = enchantManager.getRandomEnchantFromCategory(categoryId);
        if (randomEnchant == null) return mysteriousBook;

        int level = RANDOM.nextInt(randomEnchant.getMaxLevel()) + 1;
        int successRate = 50 + RANDOM.nextInt(30);
        int destroyRate = 100 - successRate;

        return createRevealedBook(randomEnchant.getId(), level, successRate, destroyRate);
    }

    @NotNull
    public ItemStack createRevealedBook(@NotNull String enchantId, int level, int successRate, int destroyRate) {
        Enchant enchant = enchantManager.getEnchant(enchantId);
        if (enchant == null) return new ItemStack(Material.AIR);

        Config config = McEnchants.getInstance().getConfiguration();
        Config applyConfig = McEnchants.getInstance().getApply();

        String material = config.getString("revealed-book-item.material", "PAPER");
        ItemStack book = new ItemStack(Material.valueOf(material.toUpperCase()));

        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;

        String romanLevel = getRomanNumeral(level);
        String name = config.getString("revealed-book-item.name", "{name} &8[&#94A3B8{level}&8]")
                .replace("{name}", enchant.getName())
                .replace("{level}", romanLevel);

        meta.setDisplayName(MessageProcessor.process(name));

        String appliesDisplay = enchant.getAppliesTo().stream()
                .map(type -> {
                    String displayKey = "applies-to-rules.displays." + type + ".display";
                    return applyConfig.getString(displayKey, type);
                })
                .collect(Collectors.joining(applyConfig.getString("applies-to-rules.separator", ", ")));

        String description = enchant.getBookLoreEnabled()
                ? String.join(" ", enchant.getBookLoreLines())
                : "";

        List<String> lore = config.getStringList("revealed-book-item.lore").stream()
                .map(line -> line
                        .replace("{description}", description)
                        .replace("{success}", String.valueOf(successRate))
                        .replace("{destroy}", String.valueOf(destroyRate))
                        .replace("{applies}", appliesDisplay))
                .map(MessageProcessor::process)
                .collect(Collectors.toList());

        meta.setLore(lore);

        int modelData = config.getInt("revealed-book-item.modeldata", 0);
        if (modelData > 0) meta.setCustomModelData(modelData);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ENCHANT_KEY, PersistentDataType.STRING, enchantId);
        pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, level);
        pdc.set(SUCCESS_KEY, PersistentDataType.INTEGER, successRate);
        pdc.set(DESTROY_KEY, PersistentDataType.INTEGER, destroyRate);
        pdc.set(REVEALED_KEY, PersistentDataType.BYTE, (byte) 1);

        book.setItemMeta(meta);
        return book;
    }

    @NotNull
    public ItemStack createDust(@NotNull String categoryId) {
        Category category = categoryManager.getCategory(categoryId);
        Config config = McEnchants.getInstance().getConfiguration();

        String material = config.getString("dust-item.material", "SUGAR");
        ItemStack dust = new ItemStack(Material.valueOf(material.toUpperCase()));

        ItemMeta meta = dust.getItemMeta();
        if (meta == null) return dust;

        int repairAmount = category.getRandomDustSuccess();

        String name = config.getString("dust-item.name", "{categoryColor}{categoryName} &fJavító Por")
                .replace("{categoryColor}", category.getColor())
                .replace("{categoryName}", category.getName());

        meta.setDisplayName(MessageProcessor.process(name));

        List<String> lore = config.getStringList("dust-item.lore").stream()
                .map(line -> line.replace("{repair}", String.valueOf(repairAmount)))
                .map(MessageProcessor::process)
                .collect(Collectors.toList());

        meta.setLore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(McEnchants.getInstance(), "dust_category"),
                PersistentDataType.STRING, categoryId);
        pdc.set(new NamespacedKey(McEnchants.getInstance(), "dust_repair"),
                PersistentDataType.INTEGER, repairAmount);

        dust.setItemMeta(meta);
        return dust;
    }

    @Nullable
    public EnchantedBook getBookData(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(REVEALED_KEY, PersistentDataType.BYTE)) return null;

        String enchantId = pdc.get(ENCHANT_KEY, PersistentDataType.STRING);
        Integer level = pdc.get(LEVEL_KEY, PersistentDataType.INTEGER);
        Integer successRate = pdc.get(SUCCESS_KEY, PersistentDataType.INTEGER);
        Integer destroyRate = pdc.get(DESTROY_KEY, PersistentDataType.INTEGER);

        if (enchantId == null || level == null || successRate == null || destroyRate == null) {
            return null;
        }

        return new EnchantedBook(enchantId, level, successRate, destroyRate, true);
    }

    public boolean isRevealedBook(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(REVEALED_KEY, PersistentDataType.BYTE);
    }

    public boolean isMysteriousBook(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer()
                .has(new NamespacedKey(McEnchants.getInstance(), "mysterious_category"),
                        PersistentDataType.STRING);
    }

    public boolean isDust(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer()
                .has(new NamespacedKey(McEnchants.getInstance(), "dust_category"),
                        PersistentDataType.STRING);
    }

    @Nullable
    public String getDustCategory(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return meta.getPersistentDataContainer()
                .get(new NamespacedKey(McEnchants.getInstance(), "dust_category"),
                        PersistentDataType.STRING);
    }

    @Nullable
    public Integer getDustRepairAmount(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return meta.getPersistentDataContainer()
                .get(new NamespacedKey(McEnchants.getInstance(), "dust_repair"),
                        PersistentDataType.INTEGER);
    }

    @NotNull
    private String getRomanNumeral(int number) {
        Config config = McEnchants.getInstance().getLevels();
        List<String> levels = config.getList("levels");

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