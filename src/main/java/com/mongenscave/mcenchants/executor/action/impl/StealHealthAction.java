package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class StealHealthAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double stealAmount = parseAmount(parts[1]);

        LivingEntity victim = null;

        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victimObj = context.get("victim");
                if (victimObj instanceof LivingEntity) victim = (LivingEntity) victimObj;
                else return;
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attackerObj = context.get("attacker");
                if (attackerObj instanceof LivingEntity) victim = (LivingEntity) attackerObj;
                else return;
            }
        } else {
            Object victimObj = context.get("victim");
            if (victimObj instanceof LivingEntity) victim = (LivingEntity) victimObj;
            else {
                Object target = context.get("target");
                if (target instanceof LivingEntity) victim = (LivingEntity) target;
                else return;
            }
        }

        if (victim == null) return;

        double targetHealth = victim.getHealth();
        double actualStolen = Math.min(stealAmount, targetHealth);

        if (actualStolen > 0) {
            victim.setHealth(Math.max(0, targetHealth - actualStolen));

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