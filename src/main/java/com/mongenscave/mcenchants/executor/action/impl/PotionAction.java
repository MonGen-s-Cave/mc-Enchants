package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PotionAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();

        String[] parts = actionString.split(":");
        if (parts.length < 4) {
            return;
        }

        String effectName = parts[1].toUpperCase();

        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) {
            return;
        }

        try {
            int amplifier = Integer.parseInt(parts[2]);
            int duration = Integer.parseInt(parts[3]);

            LivingEntity target = player;
            if (parts.length >= 5) {
                String targetSpec = parts[4].toUpperCase();
                if (targetSpec.equals("@VICTIM")) {
                    Object victim = context.get("victim");
                    if (victim instanceof LivingEntity) target = (LivingEntity) victim;
                    else return;
                } else if (targetSpec.equals("@ATTACKER")) {
                    Object attacker = context.get("attacker");
                    if (attacker instanceof LivingEntity) target = (LivingEntity) attacker;
                }
            }

            target.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, true));
        } catch (NumberFormatException exception) {
            LoggerUtil.error(exception.getMessage());
        }
    }

    @Override
    public String getActionType() {
        return "POTION";
    }
}