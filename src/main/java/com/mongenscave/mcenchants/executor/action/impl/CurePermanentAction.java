package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CurePermanentAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        String effectName = parts[1].toUpperCase();
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) return;

        LivingEntity target = player;
        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof LivingEntity) target = (LivingEntity) victim;
                else return;
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof LivingEntity) target = (LivingEntity) attacker;
            }
        }

        if (target.hasPotionEffect(effectType)) target.removePotionEffect(effectType);
    }

    @Override
    public String getActionType() {
        return "CURE_PERMANENT";
    }
}