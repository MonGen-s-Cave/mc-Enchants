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
        LoggerUtil.info("[EXP] Full action string: " + actionString);

        String[] parts = actionString.split(":", 2);
        if (parts.length < 2) {
            LoggerUtil.warn("[EXP] Missing exp amount parameter. Action string: " + actionString);
            return;
        }

        String expString = parts[1].trim();
        LoggerUtil.info("[EXP] Extracted exp string: " + expString);

        int expAmount = calculateExp(expString, context);

        LoggerUtil.info("[EXP] Calculated exp amount: " + expAmount + " from string: " + expString);

        if (expAmount > 0) {
            int expBefore = getTotalExperience(player);
            player.giveExp(expAmount);
            int expAfter = getTotalExperience(player);

            LoggerUtil.info("[EXP] Given " + expAmount + " exp to " + player.getName() + " (before: " + expBefore + ", after: " + expAfter + ")");
        } else {
            LoggerUtil.warn("[EXP] Calculated exp amount is 0 or negative: " + expAmount);
        }
    }

    @Override
    public String getActionType() {
        return "EXP";
    }

    private int calculateExp(@NotNull String expString, @NotNull Map<String, Object> context) {
        LoggerUtil.info("[EXP] Calculating from: " + expString);

        if (expString.contains("%exp%")) {
            Object expObj = context.get("exp");
            LoggerUtil.info("[EXP] Found %exp% placeholder, context value: " + expObj);

            if (expObj instanceof Number) {
                double baseExp = ((Number) expObj).doubleValue();
                String[] parts = expString.split("\\*");

                if (parts.length == 2) {
                    try {
                        double multiplier = Double.parseDouble(parts[1]);
                        int result = (int) Math.round(baseExp * multiplier);
                        LoggerUtil.info("[EXP] Multiplied " + baseExp + " by " + multiplier + " = " + result);
                        return result;
                    } catch (NumberFormatException e) {
                        LoggerUtil.warn("[EXP] Failed to parse multiplier: " + parts[1]);
                    }
                }

                int result = (int) baseExp;
                LoggerUtil.info("[EXP] Using base exp: " + result);
                return result;
            }
            LoggerUtil.warn("[EXP] exp context value is not a number");
            return 0;
        }

        if (expString.contains("-")) {
            String[] range = expString.split("-");
            if (range.length == 2) {
                try {
                    int min = Integer.parseInt(range[0].trim());
                    int max = Integer.parseInt(range[1].trim());
                    int result = ThreadLocalRandom.current().nextInt(min, max + 1);
                    LoggerUtil.info("[EXP] Random range " + min + "-" + max + " = " + result);
                    return result;
                } catch (NumberFormatException e) {
                    LoggerUtil.warn("[EXP] Failed to parse range: " + expString);
                }
            }
        }

        try {
            int result = Integer.parseInt(expString.trim());
            LoggerUtil.info("[EXP] Parsed fixed amount: " + result);
            return result;
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