package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TNTAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Object targetObj = context.get("target");
        Entity targetEntity = null;

        if (targetObj instanceof Entity) {
            targetEntity = (Entity) targetObj;
        }

        if (targetEntity == null) {
            Object victimObj = context.get("victim");
            if (victimObj instanceof Entity) {
                targetEntity = (Entity) victimObj;
            }
        }

        if (targetEntity == null) {
            targetEntity = player;
        }

        Location location = targetEntity.getLocation();
        float power = (float) actionData.multiplier();
        int radius = actionData.radius();

        float explosionPower = radius > 0 ? radius : power;

        location.getWorld().createExplosion(
                location.getX(),
                location.getY(),
                location.getZ(),
                explosionPower,
                false,
                false,
                player
        );

        location.getWorld().spawnParticle(
                Particle.EXPLOSION,
                location,
                3,
                0.5, 0.5, 0.5,
                0.1
        );

        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
    }

    @Override
    public String getActionType() {
        return "TNT";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        return context.containsKey("player");
    }
}