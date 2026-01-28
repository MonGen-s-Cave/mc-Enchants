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
        LoggerUtil.info("[DAMAGE_ARMOR] Full action string: " + actionString);

        String[] parts = actionString.split(":", 3);
        if (parts.length < 2) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Missing damage amount parameter. Action: " + actionString);
            return;
        }

        int damageAmount = parseDamageAmount(parts[1].trim());
        LoggerUtil.info("[DAMAGE_ARMOR] Parsed damage amount: " + damageAmount);

        Player target = null;

        if (parts.length >= 3) {
            String targetSpec = parts[2].toUpperCase().trim();
            LoggerUtil.info("[DAMAGE_ARMOR] Target spec: " + targetSpec);

            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Player) target = (Player) victim;
                else {
                    LoggerUtil.warn("[DAMAGE_ARMOR] Victim is not a player");
                    return;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Player) target = (Player) attacker;
                else target = player;
            }
        } else {
            Object victim = context.get("victim");
            if (victim instanceof Player) target = (Player) victim;
            else {
                LoggerUtil.warn("[DAMAGE_ARMOR] No victim found in context");
                return;
            }
        }

        if (target == null) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Target is null");
            return;
        }

        LoggerUtil.info("[DAMAGE_ARMOR] Damaging armor of: " + target.getName());

        ItemStack[] armor = target.getInventory().getArmorContents();
        boolean damaged = false;
        int piecesProcessed = 0;

        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece != null && !piece.getType().isAir()) {
                piecesProcessed++;
                LoggerUtil.info("[DAMAGE_ARMOR] Processing armor piece: " + piece.getType());

                if (damageArmorPiece(piece, damageAmount)) {
                    damaged = true;
                    LoggerUtil.info("[DAMAGE_ARMOR] Successfully damaged: " + piece.getType());

                    if (piece.getType() == Material.AIR) {
                        armor[i] = null;
                        LoggerUtil.info("[DAMAGE_ARMOR] Armor piece broke completely");
                    }
                } else {
                    LoggerUtil.warn("[DAMAGE_ARMOR] Failed to damage: " + piece.getType());
                }
            }
        }

        LoggerUtil.info("[DAMAGE_ARMOR] Processed " + piecesProcessed + " armor pieces, damaged: " + damaged);

        if (damaged) {
            target.getInventory().setArmorContents(armor);
            LoggerUtil.info("[DAMAGE_ARMOR] Updated armor contents for: " + target.getName());
        }
    }

    @Override
    public String getActionType() {
        return "DAMAGE_ARMOR";
    }

    private boolean damageArmorPiece(@NotNull ItemStack item, int damageAmount) {
        if (!(item.getItemMeta() instanceof Damageable meta)) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Item meta is not Damageable: " + item.getType());
            return false;
        }

        int currentDamage = meta.getDamage();
        int maxDurability = item.getType().getMaxDurability();

        LoggerUtil.info("[DAMAGE_ARMOR] Current damage: " + currentDamage + ", Max: " + maxDurability + ", Adding: " + damageAmount);

        if (maxDurability <= 0) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Max durability is 0 or negative");
            return false;
        }

        int newDamage = currentDamage + damageAmount;
        LoggerUtil.info("[DAMAGE_ARMOR] New damage would be: " + newDamage);

        if (newDamage >= maxDurability) {
            item.setAmount(0);
            item.setType(Material.AIR);
            LoggerUtil.info("[DAMAGE_ARMOR] Item broke (damage >= max)");
            return true;
        }

        meta.setDamage(newDamage);
        boolean success = item.setItemMeta(meta);
        LoggerUtil.info("[DAMAGE_ARMOR] Set new damage: " + newDamage + ", success: " + success);
        return success;
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