package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class KillAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Object victim = context.get("victim");
        if (!(victim instanceof LivingEntity victimEntity)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        int killCount = parseKillCount(parts[1]);

        double radius = 10.0;

        List<Entity> nearbyEntities = victimEntity.getNearbyEntities(radius, radius, radius)
                .stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> !(e instanceof Player))
                .filter(e -> e.getType() == victimEntity.getType())
                .limit(killCount)
                .toList();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity living) living.setHealth(0);
        }
    }

    @Override
    public String getActionType() {
        return "KILL";
    }

    private int parseKillCount(@NotNull String countStr) {
        if (countStr.contains("-")) {
            String[] range = countStr.split("-");
            try {
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException ignored) {}
        }

        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}