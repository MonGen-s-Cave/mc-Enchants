package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DoHarmAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double damage = parseDamage(parts[1], context);

        LivingEntity target = null;
        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof LivingEntity) {
                    target = (LivingEntity) victim;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof LivingEntity) {
                    target = (LivingEntity) attacker;
                }
            }
        } else {
            Object victim = context.get("victim");
            if (victim instanceof LivingEntity) {
                target = (LivingEntity) victim;
            } else {
                Object targetObj = context.get("target");
                if (targetObj instanceof LivingEntity) {
                    target = (LivingEntity) targetObj;
                }
            }
        }

        if (target == null) return;

        target.damage(damage, player);
    }

    @Override
    public String getActionType() {
        return "DO_HARM";
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