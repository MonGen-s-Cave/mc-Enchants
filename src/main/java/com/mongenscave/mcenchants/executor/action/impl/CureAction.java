package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CureAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity target = getTarget(context, player);
        if (!(target instanceof LivingEntity livingTarget)) return;

        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        String effectName = parts[1].toUpperCase();
        PotionEffectType effectType = PotionEffectType.getByName(effectName);

        if (effectType != null && livingTarget.hasPotionEffect(effectType)) {
            livingTarget.removePotionEffect(effectType);
        }
    }

    @Override
    public String getActionType() {
        return "CURE";
    }

    private Entity getTarget(@NotNull Map<String, Object> context, @NotNull Player defaultPlayer) {
        Object attacker = context.get("attacker");
        if (attacker instanceof Entity) return (Entity) attacker;

        Object victim = context.get("victim");
        if (victim instanceof Entity) return (Entity) victim;

        Object target = context.get("target");
        if (target instanceof Entity) return (Entity) target;

        return defaultPlayer;
    }
}