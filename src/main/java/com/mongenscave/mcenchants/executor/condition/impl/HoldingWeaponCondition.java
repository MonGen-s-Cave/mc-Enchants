package com.mongenscave.mcenchants.executor.condition.impl;

import com.mongenscave.mcenchants.executor.condition.EnchantCondition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HoldingWeaponCondition extends EnchantCondition {
    @Override
    public boolean evaluate(@NotNull Player player, @NotNull String value, @NotNull Map<String, Object> context) {
        String[] parts = value.split(":");
        if (parts.length != 2) return false;

        String weaponType = parts[0].toLowerCase();
        boolean expected = parseBoolean(parts[1]);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String typeName = mainHand.getType().name().toLowerCase();

        boolean isHolding = switch (weaponType) {
            case "sword" -> typeName.contains("_sword");
            case "axe" -> typeName.contains("_axe");
            case "bow" -> typeName.equals("bow") || typeName.equals("crossbow");
            case "pickaxe" -> typeName.contains("_pickaxe");
            case "shovel" -> typeName.contains("_shovel");
            case "hoe" -> typeName.contains("_hoe");
            default -> false;
        };

        return isHolding == expected;
    }

    @Override
    public String getConditionKey() {
        return "holding";
    }
}