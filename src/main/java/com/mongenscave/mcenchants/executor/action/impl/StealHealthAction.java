package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class StealHealthAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity target = getTarget(context);
        if (!(target instanceof LivingEntity livingTarget)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double stealAmount = parseAmount(parts[1]);

        double targetHealth = livingTarget.getHealth();
        double actualStolen = Math.min(stealAmount, targetHealth);

        if (actualStolen > 0) {
            livingTarget.setHealth(Math.max(0, targetHealth - actualStolen));

            double playerHealth = player.getHealth();
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
            double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

            player.setHealth(Math.min(maxHealth, playerHealth + actualStolen));
        }
    }

    @Override
    public String getActionType() {
        return "STEAL_HEALTH";
    }

    private @Nullable Entity getTarget(@NotNull Map<String, Object> context) {
        Object victim = context.get("victim");
        if (victim instanceof Entity) return (Entity) victim;

        Object target = context.get("target");
        if (target instanceof Entity) return (Entity) target;

        return null;
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