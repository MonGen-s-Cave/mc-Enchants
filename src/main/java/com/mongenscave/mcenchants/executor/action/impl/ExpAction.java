// ============================================
// VÉGSŐ JAVÍTÁS - ExpAction.java
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
        // ✅ FIX: A full action stringből dolgozunk
        String fullAction = actionData.fullActionString();

        if (!fullAction.contains(":")) {
            LoggerUtil.warn("[EXP] Invalid action format: " + fullAction);
            return;
        }

        String[] parts = fullAction.split(":", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            LoggerUtil.warn("[EXP] Missing exp amount in action: " + fullAction);
            return;
        }

        String expString = parts[1].trim();
        int expAmount = calculateExp(expString, context);

        if (expAmount > 0) {
            player.giveExp(expAmount);
            LoggerUtil.info("[EXP] Gave " + expAmount + " XP to player from action: " + fullAction);
        }
    }

    @Override
    public String getActionType() {
        return "EXP";
    }

    private int calculateExp(@NotNull String expString, @NotNull Map<String, Object> context) {
        // %exp% placeholder kezelés
        if (expString.contains("%exp%")) {
            Object expObj = context.get("exp");

            if (expObj instanceof Number) {
                double baseExp = ((Number) expObj).doubleValue();

                // Multiplier kezelés (pl: %exp%*1.25)
                if (expString.contains("*")) {
                    String[] parts = expString.split("\\*");
                    if (parts.length == 2) {
                        try {
                            double multiplier = Double.parseDouble(parts[1].trim());
                            int result = (int) Math.round(baseExp * multiplier);
                            LoggerUtil.info("[EXP] Calculated from context: " + baseExp + " * " + multiplier + " = " + result);
                            return result;
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

        // Range formátum (pl: 2-5)
        if (expString.contains("-")) {
            String[] range = expString.split("-");
            if (range.length == 2) {
                try {
                    int min = Integer.parseInt(range[0].trim());
                    int max = Integer.parseInt(range[1].trim());
                    int result = ThreadLocalRandom.current().nextInt(min, max + 1);
                    LoggerUtil.info("[EXP] Calculated from range " + min + "-" + max + ": " + result);
                    return result;
                } catch (NumberFormatException e) {
                    LoggerUtil.warn("[EXP] Failed to parse range: " + expString);
                }
            }
        }

        // Fix érték
        try {
            int result = Integer.parseInt(expString.trim());
            LoggerUtil.info("[EXP] Using fixed value: " + result);
            return result;
        } catch (NumberFormatException e) {
            LoggerUtil.warn("[EXP] Failed to parse exp string: " + expString);
            return 0;
        }
    }
}