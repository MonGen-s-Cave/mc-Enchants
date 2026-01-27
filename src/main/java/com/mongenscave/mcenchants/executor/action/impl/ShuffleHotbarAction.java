package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShuffleHotbarAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");

        Player target = null;
        if (parts.length >= 2) {
            String targetSpec = parts[1].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Player) target = (Player) victim;
                else return;
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Player) target = (Player) attacker;
                else return;
            }
        } else {
            Object attacker = context.get("attacker");
            if (attacker instanceof Player) target = (Player) attacker;
            else return;
        }

        if (target == null) return;

        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = target.getInventory().getItem(i);
        }

        // Fisher-Yates shuffle algoritmus
        for (int i = 8; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            ItemStack temp = hotbar[i];
            hotbar[i] = hotbar[j];
            hotbar[j] = temp;
        }

        for (int i = 0; i < 9; i++) {
            target.getInventory().setItem(i, hotbar[i]);
        }

        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }

    @Override
    public String getActionType() {
        return "SHUFFLE_HOTBAR";
    }
}