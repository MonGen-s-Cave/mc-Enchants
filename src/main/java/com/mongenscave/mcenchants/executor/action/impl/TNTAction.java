package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TNTAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        LoggerUtil.info("TNT: Starting execution");

        Entity targetEntity = (Entity) context.get("victim");
        if (targetEntity == null) {
            LoggerUtil.warn("TNT: No victim entity in context");
            return;
        }

        Location location = targetEntity.getLocation();
        int power = (int) actionData.multiplier();
        int fuseTicks = actionData.duration();
        int amount = actionData.radius();

        LoggerUtil.info("TNT: Spawning " + amount + " TNT with power " + power);

        for (int i = 0; i < amount; i++) {
            TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
            tnt.setFuseTicks(fuseTicks);
            tnt.setYield(power);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
    }

    @Override
    public String getActionType() {
        return "TNT";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        return context.containsKey("victim");
    }
}