package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class StealExpAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity victim = (Entity) context.get("victim");
        if (!(victim instanceof Player victimPlayer)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        int expAmount = parseAmount(parts[1]);
        int victimTotalExp = getTotalExperience(victimPlayer);
        int actualStolen = Math.min(expAmount, victimTotalExp);

        if (actualStolen > 0) {
            victimPlayer.setExp(0);
            victimPlayer.setLevel(0);
            victimPlayer.setTotalExperience(0);
            victimPlayer.giveExp(victimTotalExp - actualStolen);

            player.giveExp(actualStolen);
        }
    }

    @Override
    public String getActionType() {
        return "STEAL_EXP";
    }

    private int parseAmount(@NotNull String amountStr) {
        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException ignored) {}
        }

        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int getTotalExperience(@NotNull Player player) {
        int level = player.getLevel();
        int totalExp = Math.round(player.getExp() * getExpToLevel(level));

        for (int i = 0; i < level; i++) {
            totalExp += getExpToLevel(i);
        }

        return totalExp;
    }

    private int getExpToLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
}