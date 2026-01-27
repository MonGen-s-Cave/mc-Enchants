package com.mongenscave.mcenchants.executor.condition.impl;

import com.mongenscave.mcenchants.executor.condition.EnchantCondition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HasWitherCondition extends EnchantCondition {
    @Override
    public boolean evaluate(@NotNull Player player, @NotNull String value, @NotNull Map<String, Object> context) {
        boolean expected = parseBoolean(value);

        Object victim = context.get("victim");
        if (victim instanceof LivingEntity) {
            return ((LivingEntity) victim).hasPotionEffect(PotionEffectType.WITHER) == expected;
        }

        Object target = context.get("target");
        if (target instanceof LivingEntity) {
            return ((LivingEntity) target).hasPotionEffect(PotionEffectType.WITHER) == expected;
        }

        return player.hasPotionEffect(PotionEffectType.WITHER) == expected;
    }

    @Override
    public String getConditionKey() {
        return "has_wither";
    }
}