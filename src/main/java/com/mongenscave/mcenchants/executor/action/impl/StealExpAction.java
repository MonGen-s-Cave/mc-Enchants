// ============================================
// KRITIKUS JAVÍTÁS - StealExpAction.java
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
        String actionString = actionData.fullActionString();
        LoggerUtil.info("[STEAL_EXP] Full action string: " + actionString);

        String[] parts = actionString.split(":", 3);
        if (parts.length < 2) {
            LoggerUtil.warn("[STEAL_EXP] Missing exp amount parameter. Action: " + actionString);
            return;
        }

        int expAmount = parseAmount(parts[1].trim());
        LoggerUtil.info("[STEAL_EXP] Parsed exp amount: " + expAmount);

        Player victim = null;

        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase().trim();
            LoggerUtil.info("[STEAL_EXP] Target spec: " + targetSpec);

            if (targetSpec.equals("@VICTIM")) {
                Object victimObj = context.get("victim");
                if (victimObj instanceof Player) victim = (Player) victimObj;
                else {
                    LoggerUtil.warn("[STEAL_EXP] @VICTIM specified but victim is not a player");
                    return;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attackerObj = context.get("attacker");
                if (attackerObj instanceof Player) victim = (Player) attackerObj;
                else {
                    LoggerUtil.warn("[STEAL_EXP] @ATTACKER specified but attacker is not a player");
                    return;
                }
            }
        } else {
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

        LoggerUtil.info("[STEAL_EXP] Stealing " + expAmount + " exp from " + victim.getName() + " to " + attacker.getName());

        int victimTotalExp = getTotalExperience(victim);
        LoggerUtil.info("[STEAL_EXP] Victim total exp: " + victimTotalExp);

        int actualStolen = Math.min(expAmount, victimTotalExp);
        LoggerUtil.info("[STEAL_EXP] Actually stealing: " + actualStolen);

        if (actualStolen > 0) {
            int newVictimExp = victimTotalExp - actualStolen;
            setTotalExperience(victim, newVictimExp);
            LoggerUtil.info("[STEAL_EXP] Victim new exp: " + newVictimExp);

            int attackerBefore = getTotalExperience(attacker);
            attacker.giveExp(actualStolen);
            int attackerAfter = getTotalExperience(attacker);
            LoggerUtil.info("[STEAL_EXP] Attacker exp: " + attackerBefore + " -> " + attackerAfter);
        } else {
            LoggerUtil.warn("[STEAL_EXP] No exp stolen (victim has 0 exp)");
        }
    }

    @Override
    public String getActionType() {
        return "STEAL_EXP";
    }

    private int parseAmount(@NotNull String amountStr) {
        amountStr = amountStr.trim();

        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0].trim());
                int max = Integer.parseInt(range[1].trim());
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException e) {
                LoggerUtil.warn("[STEAL_EXP] Failed to parse range: " + amountStr);
            }
        }

        try {
            return Integer.parseInt(amountStr);
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