package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class IncreaseDamageAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        double increasePercent = parsePercentage(parts[1]);

        double currentDamage = damageEvent.getDamage();
        double increase = currentDamage * (increasePercent / 100.0);
        damageEvent.setDamage(currentDamage + increase);
    }

    @Override
    public String getActionType() {
        return "INCREASE_DAMAGE";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        return event instanceof EntityDamageByEntityEvent;
    }

    private double parsePercentage(@NotNull String percentStr) {
        if (percentStr.contains("-")) {
            String[] range = percentStr.split("-");
            try {
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException ignored) {}
        }

        try {
            return Double.parseDouble(percentStr);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }
}