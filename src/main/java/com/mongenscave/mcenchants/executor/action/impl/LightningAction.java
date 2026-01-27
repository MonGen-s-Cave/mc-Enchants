package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LightningAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");

        Entity target = null;
        if (parts.length >= 2) {
            String targetSpec = parts[1].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Entity) target = (Entity) victim;
                else return;
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Entity) target = (Entity) attacker;
                else target = player;
            }
        } else {
            Object victim = context.get("victim");
            if (victim instanceof Entity) target = (Entity) victim;
            else {
                Object targetObj = context.get("target");
                if (targetObj instanceof Entity) target = (Entity) targetObj;
                else return;
            }
        }

        if (target == null) return;

        Location location = target.getLocation();
        location.getWorld().strikeLightning(location);
    }

    @Override
    public String getActionType() {
        return "LIGHTNING";
    }
}