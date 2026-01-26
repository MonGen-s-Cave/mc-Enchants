package com.mongenscave.mcenchants.model;

import com.mongenscave.mcenchants.identifier.EnchantType;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Enchant {
    @NotNull private final String id;
    @NotNull private final String name;
    @NotNull private final Category category;
    @NotNull private final List<String> appliesTo;
    @NotNull private final EnchantType type;

    private final boolean itemLoreEnabled;
    @NotNull private final List<String> itemLoreLines;

    private final boolean bookLoreEnabled;
    @NotNull private final List<String> bookLoreLines;

    @NotNull private final Map<Integer, EnchantLevel> levels;

    public boolean canApplyTo(@NotNull ItemStack item) {
        String materialName = item.getType().name().toLowerCase();

        return appliesTo.stream().anyMatch(type -> {
            if (materialName.contains(type.toLowerCase())) return true;

            return switch (type.toLowerCase()) {
                case "armor" -> materialName.contains("helmet") || materialName.contains("chestplate")
                        || materialName.contains("leggings") || materialName.contains("boots");
                case "tool" -> materialName.contains("pickaxe") || materialName.contains("axe")
                        || materialName.contains("shovel") || materialName.contains("hoe");
                case "weapon" -> materialName.contains("sword") || materialName.contains("axe");
                default -> false;
            };
        });
    }

    public EnchantLevel getLevel(int level) {
        return levels.get(level);
    }

    public int getMaxLevel() {
        return levels.size();
    }
}