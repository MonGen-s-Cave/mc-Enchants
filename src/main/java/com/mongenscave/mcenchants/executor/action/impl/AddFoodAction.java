package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AddFoodAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        int foodAmount = parseAmount(parts[1]);

        Player target = player;
        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Player) {
                    target = (Player) victim;
                } else {
                    return;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Player) {
                    target = (Player) attacker;
                }
            }
        }

        int currentFood = target.getFoodLevel();
        int newFood = Math.min(20, currentFood + foodAmount);
        target.setFoodLevel(newFood);

        float saturation = target.getSaturation();
        target.setSaturation(Math.min(20f, saturation + foodAmount * 0.6f));
    }

    @Override
    public String getActionType() {
        return "ADD_FOOD";
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
            return 1;
        }
    }
}