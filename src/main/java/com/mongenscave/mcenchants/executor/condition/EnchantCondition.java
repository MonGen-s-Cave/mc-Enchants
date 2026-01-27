package com.mongenscave.mcenchants.executor.condition;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class EnchantCondition {
    public abstract boolean evaluate(@NotNull Player player, @NotNull String value, @NotNull Map<String, Object> context);

    public abstract String getConditionKey();

    protected boolean parseBoolean(@NotNull String value) {
        return value.equalsIgnoreCase("true");
    }

    protected int parseInt(@NotNull String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected double parseDouble(@NotNull String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}