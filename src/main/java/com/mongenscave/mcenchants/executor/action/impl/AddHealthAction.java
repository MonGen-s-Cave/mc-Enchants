package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AddHealthAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity target = getTarget(context, player);
        if (!(target instanceof LivingEntity livingTarget)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double healthAmount = parseAmount(parts[1]);

        double currentHealth = livingTarget.getHealth();
        AttributeInstance maxHealthAttr = livingTarget.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

        double newHealth = Math.min(maxHealth, currentHealth + healthAmount);
        livingTarget.setHealth(newHealth);
    }

    @Override
    public String getActionType() {
        return "ADD_HEALTH";
    }

    private Entity getTarget(@NotNull Map<String, Object> context, @NotNull Player defaultPlayer) {
        Object victim = context.get("victim");
        if (victim instanceof Entity) return (Entity) victim;

        Object target = context.get("target");
        if (target instanceof Entity) return (Entity) target;

        return defaultPlayer;
    }

    private double parseAmount(@NotNull String amountStr) {
        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException ignored) {}
        }

        try {
            return Double.parseDouble(amountStr);
        } catch (NumberFormatException ignored) {
            return 1.0;
        }
    }
}