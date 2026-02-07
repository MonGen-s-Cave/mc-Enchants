package com.mongenscave.mcenchants.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class EnchantRemoverTable {
    private final @NotNull Location location;
    private @Nullable ItemStack placedItem;
    private @Nullable ItemDisplay itemDisplay;
    private final Map<String, Integer> remainingEnchants = new HashMap<>();
    private final Map<String, Integer> swipeProgress = new HashMap<>();

    public EnchantRemoverTable(@NotNull Location location) {
        this.location = location;
    }

    public void addEnchant(@NotNull String enchantId, int level) {
        remainingEnchants.put(enchantId, level);
        swipeProgress.put(enchantId, 0);
    }

    public boolean hasEnchants() {
        return !remainingEnchants.isEmpty();
    }

    public void incrementSwipe(@NotNull String enchantId) {
        swipeProgress.put(enchantId, swipeProgress.getOrDefault(enchantId, 0) + 1);
    }

    public int getSwipeProgress(@NotNull String enchantId) {
        return swipeProgress.getOrDefault(enchantId, 0);
    }

    public void removeEnchant(@NotNull String enchantId) {
        remainingEnchants.remove(enchantId);
        swipeProgress.remove(enchantId);
    }

    @Nullable
    public String getNextEnchant() {
        return remainingEnchants.keySet().stream().findFirst().orElse(null);
    }
}