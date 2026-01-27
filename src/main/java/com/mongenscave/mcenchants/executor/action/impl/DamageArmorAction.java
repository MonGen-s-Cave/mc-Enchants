package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DamageArmorAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        int damageAmount = parseDamageAmount(parts[1]);

        Player target = null;
        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Player) target = (Player) victim;
                else return;
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Player) target = (Player) attacker;
                else target = player;
            }
        } else {
            Object victim = context.get("victim");
            if (victim instanceof Player) target = (Player) victim;
            else return;
        }

        if (target == null) return;

        ItemStack[] armor = target.getInventory().getArmorContents();
        boolean damaged = false;

        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece != null && !piece.getType().isAir()) {
                if (damageArmorPiece(piece, damageAmount)) {
                    damaged = true;
                    if (piece.getType() == Material.AIR) {
                        armor[i] = null;
                    }
                }
            }
        }

        if (damaged) {
            target.getInventory().setArmorContents(armor);
        }
    }

    @Override
    public String getActionType() {
        return "DAMAGE_ARMOR";
    }

    private boolean damageArmorPiece(@NotNull ItemStack item, int damageAmount) {
        if (!(item.getItemMeta() instanceof Damageable meta)) {
            return false;
        }

        int currentDamage = meta.getDamage();
        int maxDurability = item.getType().getMaxDurability();

        if (maxDurability <= 0) {
            return false;
        }

        int newDamage = currentDamage + damageAmount;

        if (newDamage >= maxDurability) {
            item.setAmount(0);
            return true;
        }

        meta.setDamage(newDamage);
        item.setItemMeta(meta);
        return true;
    }

    private int parseDamageAmount(@NotNull String amountStr) {
        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException ignored) {}
        }

        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}