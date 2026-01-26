package com.mongenscave.mcenchants.manager;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.identifier.EnchantType;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantAction;
import com.mongenscave.mcenchants.model.EnchantLevel;
import com.mongenscave.mcenchants.util.LoggerUtil;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
public class EnchantManager {
    private final Map<String, Enchant> enchants = new ConcurrentHashMap<>();
    private final CategoryManager categoryManager;

    public EnchantManager(@NotNull CategoryManager categoryManager) {
        this.categoryManager = categoryManager;
        loadEnchants();
    }

    public void loadEnchants() {
        enchants.clear();

        Config config = McEnchants.getInstance().getEnchants();
        Section enchantsSection = config.getSection("enchants");

        if (enchantsSection == null) {
            LoggerUtil.warn("0 enchants!");
            return;
        }

        for (String enchantId : enchantsSection.getRoutesAsStrings(false)) {
            Section enchantSection = enchantsSection.getSection(enchantId);
            if (enchantSection == null) continue;

            try {
                Enchant enchant = loadEnchant(enchantId, enchantSection);
                enchants.put(enchantId, enchant);
            } catch (Exception exception) {
                LoggerUtil.error(exception.getMessage());
            }
        }
    }

    @NotNull
    private Enchant loadEnchant(@NotNull String id, @NotNull Section section) {
        String name = section.getString("name", id);
        String categoryId = section.getString("category", "common");

        List<String> appliesTo = Arrays.stream(section.getString("applies-to", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        EnchantType type = EnchantType.valueOf(section.getString("type", "RIGHT_CLICK").toUpperCase());

        Section itemLoreSection = section.getSection("item-lore");
        boolean itemLoreEnabled = itemLoreSection != null && itemLoreSection.getBoolean("enabled", false);
        List<String> itemLoreLines = itemLoreEnabled ? itemLoreSection.getStringList("lines") : Collections.emptyList();

        Section bookLoreSection = section.getSection("book-lore");
        boolean bookLoreEnabled = bookLoreSection != null && bookLoreSection.getBoolean("enabled", false);
        List<String> bookLoreLines = bookLoreEnabled ? bookLoreSection.getStringList("lines") : Collections.emptyList();

        Map<Integer, EnchantLevel> levels = new HashMap<>();
        Section levelsSection = section.getSection("levels");

        if (levelsSection != null) {
            for (String levelStr : levelsSection.getRoutesAsStrings(false)) {
                try {
                    int level = Integer.parseInt(levelStr);
                    Section levelSection = levelsSection.getSection(levelStr);

                    if (levelSection != null) {
                        EnchantLevel enchantLevel = loadEnchantLevel(level, levelSection);
                        levels.put(level, enchantLevel);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return Enchant.builder()
                .id(id)
                .name(name)
                .category(categoryManager.getCategory(categoryId))
                .appliesTo(appliesTo)
                .type(type)
                .itemLoreEnabled(itemLoreEnabled)
                .itemLoreLines(itemLoreLines)
                .bookLoreEnabled(bookLoreEnabled)
                .bookLoreLines(bookLoreLines)
                .levels(levels)
                .build();
    }

    @NotNull
    private EnchantLevel loadEnchantLevel(int level, @NotNull Section section) {
        List<String> conditions = section.getStringList("conditions");
        long cooldown = section.getLong("cooldown", 0L);
        int chance = section.getInt("chance", 100);

        List<EnchantAction> actions = section.getStringList("actions").stream()
                .map(EnchantAction::fromString)
                .collect(Collectors.toList());

        return EnchantLevel.builder()
                .level(level)
                .chance(chance)
                .conditions(conditions)
                .cooldown(cooldown)
                .actions(actions)
                .build();
    }

    @Nullable
    public Enchant getEnchant(@NotNull String id) {
        return enchants.get(id);
    }

    @NotNull
    public Collection<Enchant> getEnchantsByCategory(@NotNull String categoryId) {
        return enchants.values().stream()
                .filter(e -> e.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());
    }

    @Nullable
    public Enchant getRandomEnchantFromCategory(@NotNull String categoryId) {
        List<Enchant> categoryEnchants = new ArrayList<>(getEnchantsByCategory(categoryId));

        if (categoryEnchants.isEmpty()) return null;

        return categoryEnchants.get(ThreadLocalRandom.current().nextInt(categoryEnchants.size()));
    }

    public void reload() {
        loadEnchants();
    }
}