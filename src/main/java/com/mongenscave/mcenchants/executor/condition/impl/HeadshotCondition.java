package com.mongenscave.mcenchants.executor.condition.impl;

import com.mongenscave.mcenchants.executor.condition.EnchantCondition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HeadshotCondition extends EnchantCondition {
    @Override
    public boolean evaluate(@NotNull Player player, @NotNull String value, @NotNull Map<String, Object> context) {
        boolean expected = parseBoolean(value);

        Object headshotObj = context.get("headshot");
        boolean isHeadshot = headshotObj instanceof Boolean && (Boolean) headshotObj;

        return isHeadshot == expected;
    }

    @Override
    public String getConditionKey() {
        return "headshot";
    }
}