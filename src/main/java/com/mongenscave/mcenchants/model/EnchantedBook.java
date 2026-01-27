package com.mongenscave.mcenchants.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class EnchantedBook {
    @NotNull private final String enchantId;
    private final int level;
    private int successRate;
    private int destroyRate;
    private boolean revealed;

    public void repair(int amount) {
        this.successRate = Math.min(100, this.successRate + amount);
        this.destroyRate = Math.max(0, this.destroyRate - amount);
    }

    public boolean isValid() {
        return successRate + destroyRate == 100;
    }
}