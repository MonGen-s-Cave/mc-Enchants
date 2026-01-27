package com.mongenscave.mcenchants.executor.condition.impl;

import com.mongenscave.mcenchants.executor.condition.EnchantCondition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HealthCondition extends EnchantCondition {
    @Override
    public boolean evaluate(@NotNull Player player, @NotNull String value, @NotNull Map<String, Object> context) {
        double playerHealth = player.getHealth();

        value = value.trim();

        if (value.startsWith(">")) {
            double threshold = parseDouble(value.substring(1).trim(), 0);
            return playerHealth > threshold;
        } else if (value.startsWith("<")) {
            double threshold = parseDouble(value.substring(1).trim(), 0);
            return playerHealth < threshold;
        } else if (value.startsWith("=")) {
            double threshold = parseDouble(value.substring(1).trim(), 0);
            return Math.abs(playerHealth - threshold) < 0.01;
        } else {
            double threshold = parseDouble(value, 0);
            return Math.abs(playerHealth - threshold) < 0.01;
        }
    }

    @Override
    public String getConditionKey() {
        return "health";
    }
}