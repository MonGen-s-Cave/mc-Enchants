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
    private @NotNull Location location;
    private @Nullable ItemStack placedItem;
    private @Nullable ItemDisplay itemDisplay;
    private final Map<String, Integer> remainingEnchants = new HashMap<>();
    private long placedTimestamp = 0;

    public EnchantRemoverTable(@NotNull Location location) {
        this.location = location;
    }

    public void addEnchant(@NotNull String enchantId, int level) {
        remainingEnchants.put(enchantId, level);
    }

    public boolean hasEnchants() {
        return !remainingEnchants.isEmpty();
    }

    public void removeEnchant(@NotNull String enchantId) {
        remainingEnchants.remove(enchantId);
    }

    @Nullable
    public String getNextEnchant() {
        return remainingEnchants.keySet().stream().findFirst().orElse(null);
    }

    public void setPlacedItem(@Nullable ItemStack item) {
        this.placedItem = item;
        this.placedTimestamp = item != null ? System.currentTimeMillis() : 0;
    }

    public boolean isExpired(long timeoutMillis) {
        return placedItem != null && placedTimestamp > 0
                && (System.currentTimeMillis() - placedTimestamp) >= timeoutMillis;
    }
}