package com.mongenscave.mcenchants.executor.condition.impl;

import com.mongenscave.mcenchants.executor.condition.EnchantCondition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HoldingAxeCondition extends EnchantCondition {
    @Override
    public boolean evaluate(@NotNull Player player, @NotNull String value, @NotNull Map<String, Object> context) {
        boolean expected = parseBoolean(value);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String typeName = mainHand.getType().name();
        boolean holdingAxe = typeName.contains("_AXE");

        return holdingAxe == expected;
    }

    @Override
    public String getConditionKey() {
        return "holding_axe";
    }
}