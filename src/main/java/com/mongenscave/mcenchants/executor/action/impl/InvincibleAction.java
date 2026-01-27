package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InvincibleAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        boolean isActive = context.containsKey("invincible_active");

        if (!isActive) {
            LoggerUtil.info("INVINCIBLE: Setting player invulnerable");
            player.setInvulnerable(true);
            context.put("invincible_active", true);
        } else {
            LoggerUtil.info("INVINCIBLE: Removing invulnerability");
            player.setInvulnerable(false);
            context.remove("invincible_active");
        }
    }

    @Override
    public String getActionType() {
        return "INVINCIBLE";
    }
}