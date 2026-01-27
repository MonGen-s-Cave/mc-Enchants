package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NegateDamageAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        EntityDamageEvent damageEvent = null;

        if (event instanceof EntityDamageByEntityEvent) {
            damageEvent = (EntityDamageByEntityEvent) event;
        } else if (event instanceof EntityDamageEvent) {
            damageEvent = (EntityDamageEvent) event;
        }

        if (damageEvent == null) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double negateAmount = parseAmount(parts[1]);

        double currentDamage = damageEvent.getDamage();
        double newDamage = Math.max(0, currentDamage - negateAmount);
        damageEvent.setDamage(newDamage);
    }

    @Override
    public String getActionType() {
        return "NEGATE_DAMAGE";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        return event instanceof EntityDamageEvent;
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
            return 0.0;
        }
    }
}