package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PotionAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        LoggerUtil.info("POTION: Parsing action string: " + actionString);

        String[] parts = actionString.split(":");
        if (parts.length < 4) {
            LoggerUtil.warn("POTION: Invalid format. Expected POTION:EFFECT:LEVEL:DURATION, got: " + actionString);
            return;
        }

        String effectName = parts[1].toUpperCase();
        LoggerUtil.info("POTION: Effect name: " + effectName);

        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) {
            LoggerUtil.warn("POTION: Unknown effect type: " + effectName);
            return;
        }

        try {
            int amplifier = Integer.parseInt(parts[2]);
            int duration = Integer.parseInt(parts[3]);

            LoggerUtil.info("POTION: Applying " + effectName + " level " + amplifier + " for " + duration + " ticks to player");

            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, true));
            LoggerUtil.info("POTION: Successfully applied effect to player");
        } catch (NumberFormatException e) {
            LoggerUtil.warn("POTION: Invalid level or duration format");
        }
    }

    @Override
    public String getActionType() {
        return "POTION";
    }
}