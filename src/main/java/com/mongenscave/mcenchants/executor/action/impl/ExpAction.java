package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ExpAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        String expString = parts[1];
        int expAmount = calculateExp(expString, context);

        if (expAmount > 0) {
            player.giveExp(expAmount);
        }
    }

    @Override
    public String getActionType() {
        return "EXP";
    }

    private int calculateExp(@NotNull String expString, @NotNull Map<String, Object> context) {
        if (expString.contains("%exp%")) {
            Object expObj = context.get("exp");
            if (expObj instanceof Number) {
                double baseExp = ((Number) expObj).doubleValue();

                String[] parts = expString.split("\\*");
                if (parts.length == 2) {
                    try {
                        double multiplier = Double.parseDouble(parts[1]);
                        return (int) Math.round(baseExp * multiplier);
                    } catch (NumberFormatException ignored) {}
                }

                return (int) baseExp;
            }
            return 0;
        }

        if (expString.contains("-")) {
            String[] range = expString.split("-");
            if (range.length == 2) {
                try {
                    int min = Integer.parseInt(range[0]);
                    int max = Integer.parseInt(range[1]);
                    return ThreadLocalRandom.current().nextInt(min, max + 1);
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            return Integer.parseInt(expString);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}