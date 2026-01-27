package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DoHarmAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity target = getTarget(context);
        if (!(target instanceof LivingEntity livingTarget)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double damage = parseDamage(parts[1], context);

        livingTarget.damage(damage, player);
    }

    @Override
    public String getActionType() {
        return "DO_HARM";
    }

    private @Nullable Entity getTarget(@NotNull Map<String, Object> context) {
        Object victim = context.get("victim");
        if (victim instanceof Entity) return (Entity) victim;

        Object target = context.get("target");
        if (target instanceof Entity) return (Entity) target;

        Object attacker = context.get("attacker");
        if (attacker instanceof Entity) return (Entity) attacker;

        return null;
    }

    private double parseDamage(@NotNull String damageStr, @NotNull Map<String, Object> context) {
        if (damageStr.equals("%damage%")) {
            Object damageObj = context.get("damage");
            if (damageObj instanceof Number) {
                return ((Number) damageObj).doubleValue();
            }
            return 1.0;
        }

        if (damageStr.contains("-")) {
            String[] range = damageStr.split("-");
            try {
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException ignored) {}
        }

        try {
            return Double.parseDouble(damageStr);
        } catch (NumberFormatException ignored) {
            return 1.0;
        }
    }
}