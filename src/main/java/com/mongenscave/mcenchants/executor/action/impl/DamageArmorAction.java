// ============================================
// KRITIKUS JAVÍTÁS - DamageArmorAction.java
// ============================================

package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
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

        // Ellenőrizzük hogy van-e "DAMAGE_ARMOR:" prefix
        if (!actionString.toUpperCase().startsWith("DAMAGE_ARMOR:")) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Invalid action format. Expected 'DAMAGE_ARMOR:amount' but got: " + actionString);
            return;
        }

        String[] parts = actionString.split(":", 3);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Missing damage amount parameter. Action: " + actionString);
            return;
        }

        int damageAmount = parseDamageAmount(parts[1].trim());

        Player target = null;

        // Ha van target specifikáció (@VICTIM vagy @ATTACKER)
        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase().trim();

            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Player) {
                    target = (Player) victim;
                } else {
                    LoggerUtil.warn("[DAMAGE_ARMOR] Victim is not a player");
                    return;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Player) {
                    target = (Player) attacker;
                } else {
                    target = player;
                }
            }
        } else {
            // Ha nincs target spec, akkor alapból a victim
            Object victim = context.get("victim");
            if (victim instanceof Player) {
                target = (Player) victim;
            } else {
                LoggerUtil.warn("[DAMAGE_ARMOR] No victim found in context");
                return;
            }
        }

        if (target == null) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Target is null");
            return;
        }

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
            item.setType(Material.AIR);
            return true;
        }

        meta.setDamage(newDamage);
        return item.setItemMeta(meta);
    }

    private int parseDamageAmount(@NotNull String amountStr) {
        amountStr = amountStr.trim();

        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0].trim());
                int max = Integer.parseInt(range[1].trim());
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException e) {
                LoggerUtil.warn("[DAMAGE_ARMOR] Failed to parse range: " + amountStr);
            }
        }

        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Failed to parse amount: " + amountStr);
            return 1;
        }
    }
}