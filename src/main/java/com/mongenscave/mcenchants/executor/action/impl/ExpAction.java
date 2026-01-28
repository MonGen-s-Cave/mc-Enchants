// ============================================
// KRITIKUS JAVÍTÁS - ExpAction.java
// ============================================

package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ExpAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();

        // Ellenőrizzük hogy van-e "EXP:" prefix
        if (!actionString.toUpperCase().startsWith("EXP:")) {
            LoggerUtil.warn("[EXP] Invalid action format. Expected 'EXP:amount' but got: " + actionString);
            return;
        }

        String[] parts = actionString.split(":", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            LoggerUtil.warn("[EXP] Missing exp amount parameter. Action string: " + actionString);
            return;
        }

        String expString = parts[1].trim();
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
        // Ha %exp% placeholder van benne
        if (expString.contains("%exp%")) {
            Object expObj = context.get("exp");

            if (expObj instanceof Number) {
                double baseExp = ((Number) expObj).doubleValue();

                // Ha van szorzó (pl: %exp%*1.25)
                if (expString.contains("*")) {
                    String[] parts = expString.split("\\*");
                    if (parts.length == 2) {
                        try {
                            double multiplier = Double.parseDouble(parts[1]);
                            return (int) Math.round(baseExp * multiplier);
                        } catch (NumberFormatException e) {
                            LoggerUtil.warn("[EXP] Failed to parse multiplier in: " + expString);
                        }
                    }
                }

                return (int) baseExp;
            }

            LoggerUtil.warn("[EXP] exp context value is not a number");
            return 0;
        }

        // Ha range formátum (pl: 2-5)
        if (expString.contains("-")) {
            String[] range = expString.split("-");
            if (range.length == 2) {
                try {
                    int min = Integer.parseInt(range[0].trim());
                    int max = Integer.parseInt(range[1].trim());
                    return ThreadLocalRandom.current().nextInt(min, max + 1);
                } catch (NumberFormatException e) {
                    LoggerUtil.warn("[EXP] Failed to parse range: " + expString);
                }
            }
        }

        // Fix érték
        try {
            return Integer.parseInt(expString.trim());
        } catch (NumberFormatException e) {
            LoggerUtil.warn("[EXP] Failed to parse exp string: " + expString);
            return 0;
        }
    }

    private int getTotalExperience(@NotNull Player player) {
        int level = player.getLevel();
        int exp = Math.round(player.getExp() * getExpToLevel(level));

        for (int i = 0; i < level; i++) {
            exp += getExpToLevel(i);
        }

        return exp;
    }

    private int getExpToLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        else if (level <= 30) return 5 * level - 38;
        else return 9 * level - 158;
    }
}