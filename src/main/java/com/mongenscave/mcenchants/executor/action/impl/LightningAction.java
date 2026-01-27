package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class LightningAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity target = getTarget(context);
        if (target == null) return;

        Location location = target.getLocation();
        location.getWorld().strikeLightning(location);
    }

    @Override
    public String getActionType() {
        return "LIGHTNING";
    }

    private @Nullable Entity getTarget(@NotNull Map<String, Object> context) {
        Object victim = context.get("victim");
        if (victim instanceof Entity) {
            return (Entity) victim;
        }

        Object target = context.get("target");
        if (target instanceof Entity) {
            return (Entity) target;
        }

        return null;
    }
}