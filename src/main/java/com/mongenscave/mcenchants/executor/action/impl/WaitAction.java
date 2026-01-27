package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WaitAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        LoggerUtil.info("WAIT: Checking invincibility status");

        if (context.containsKey("invincible_active")) {
            player.setInvulnerable(false);
            context.remove("invincible_active");
            LoggerUtil.info("WAIT: Removed invincibility");
        }
    }

    @Override
    public String getActionType() {
        return "WAIT";
    }
}