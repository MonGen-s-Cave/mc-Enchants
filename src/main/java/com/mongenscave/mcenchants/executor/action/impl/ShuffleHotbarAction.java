package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShuffleHotbarAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        LoggerUtil.info("SHUFFLE_HOTBAR: Starting execution");

        Entity attacker = (Entity) context.get("attacker");
        if (!(attacker instanceof Player attackerPlayer)) {
            LoggerUtil.warn("SHUFFLE_HOTBAR: Attacker is not a player");
            return;
        }

        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = attackerPlayer.getInventory().getItem(i);
        }

        // Fisher-Yates shuffle algoritms
        for (int i = 8; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            ItemStack temp = hotbar[i];
            hotbar[i] = hotbar[j];
            hotbar[j] = temp;
        }

        for (int i = 0; i < 9; i++) {
            attackerPlayer.getInventory().setItem(i, hotbar[i]);
        }

        attackerPlayer.playSound(attackerPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
        LoggerUtil.info("SHUFFLE_HOTBAR: Hotbar shuffled");
    }

    @Override
    public String getActionType() {
        return "SHUFFLE_HOTBAR";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        Entity attacker = (Entity) context.get("attacker");
        return attacker instanceof Player;
    }
}