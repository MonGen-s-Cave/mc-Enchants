// ============================================
// VÉGSŐ JAVÍTÁS - StealExpAction.java
// ============================================

package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class StealExpAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player attacker, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        // ✅ FIX: A full action stringből dolgozunk
        String fullAction = actionData.fullActionString();

        if (!fullAction.contains(":")) {
            LoggerUtil.warn("[STEAL_EXP] Invalid action format: " + fullAction);
            return;
        }

        String[] parts = fullAction.split(":", 3);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            LoggerUtil.warn("[STEAL_EXP] Missing exp amount in action: " + fullAction);
            return;
        }

        int expAmount = parseAmount(parts[1].trim());

        Player victim = null;

        // Target meghatározása
        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase().trim();

            if (targetSpec.equals("@VICTIM")) {
                Object victimObj = context.get("victim");
                if (victimObj instanceof Player) {
                    victim = (Player) victimObj;
                } else {
                    LoggerUtil.warn("[STEAL_EXP] @VICTIM specified but victim is not a player");
                    return;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attackerObj = context.get("attacker");
                if (attackerObj instanceof Player) {
                    victim = (Player) attackerObj;
                } else {
                    LoggerUtil.warn("[STEAL_EXP] @ATTACKER specified but attacker is not a player");
                    return;
                }
            }
        } else {
            // Alapértelmezett: victim
            Object victimObj = context.get("victim");
            if (victimObj instanceof Player) {
                victim = (Player) victimObj;
            } else {
                LoggerUtil.warn("[STEAL_EXP] No victim found in context");
                return;
            }
        }

        if (victim == null) {
            LoggerUtil.warn("[STEAL_EXP] Victim is null");
            return;
        }

        int victimTotalExp = getTotalExperience(victim);
        int actualStolen = Math.min(expAmount, victimTotalExp);

        if (actualStolen > 0) {
            int newVictimExp = victimTotalExp - actualStolen;
            setTotalExperience(victim, newVictimExp);
            attacker.giveExp(actualStolen);

            LoggerUtil.info("[STEAL_EXP] Stole " + actualStolen + " XP from " + victim.getName() +
                    " to " + attacker.getName() + " (from action: " + fullAction + ")");
        }
    }

    @Override
    public String getActionType() {
        return "STEAL_EXP";
    }

    private int parseAmount(@NotNull String amountStr) {
        amountStr = amountStr.trim();

        // Range támogatás (pl: "25-125")
        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0].trim());
                int max = Integer.parseInt(range[1].trim());
                int result = ThreadLocalRandom.current().nextInt(min, max + 1);
                LoggerUtil.info("[STEAL_EXP] Calculated from range " + min + "-" + max + ": " + result);
                return result;
            } catch (NumberFormatException e) {
                LoggerUtil.warn("[STEAL_EXP] Failed to parse range: " + amountStr);
            }
        }

        // Fix érték
        try {
            int result = Integer.parseInt(amountStr);
            LoggerUtil.info("[STEAL_EXP] Using fixed value: " + result);
            return result;
        } catch (NumberFormatException e) {
            LoggerUtil.warn("[STEAL_EXP] Failed to parse amount: " + amountStr);
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

    private void setTotalExperience(@NotNull Player player, int exp) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        if (exp > 0) player.giveExp(exp);
    }

    private int getExpToLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        else if (level <= 30) return 5 * level - 38;
        else return 9 * level - 158;
    }
}