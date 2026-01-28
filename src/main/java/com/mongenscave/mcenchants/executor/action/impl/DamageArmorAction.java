// ============================================
// VÉGSŐ JAVÍTÁS - DamageArmorAction.java
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
        // ✅ FIX: Közvetlenül az actionData-ból szerezzük az értéket
        int damageAmount;

        // Ha van multiplier érték, azt használjuk
        if (actionData.multiplier() > 1.0) {
            damageAmount = (int) actionData.multiplier();
        }
        // Ha van radius érték, azt használjuk
        else if (actionData.radius() > 0) {
            damageAmount = actionData.radius();
        }
        // Különben próbáljuk meg a full stringből kinyerni
        else {
            String fullAction = actionData.fullActionString();
            damageAmount = parseAmountFromString(fullAction);
        }

        if (damageAmount <= 0) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Invalid damage amount: " + damageAmount);
            return;
        }

        Player target = null;
        String fullAction = actionData.fullActionString();

        // Target meghatározása
        if (fullAction.contains(":")) {
            String[] parts = fullAction.split(":");
            if (parts.length >= 3) {
                String targetSpec = parts[2].toUpperCase().trim();
                if (targetSpec.equals("@VICTIM")) {
                    Object victim = context.get("victim");
                    if (victim instanceof Player) {
                        target = (Player) victim;
                    }
                } else if (targetSpec.equals("@ATTACKER")) {
                    target = player;
                }
            }
        }

        // Ha nincs explicit target, victim az alapértelmezett
        if (target == null) {
            Object victim = context.get("victim");
            if (victim instanceof Player) {
                target = (Player) victim;
            } else {
                return;
            }
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
            LoggerUtil.info("[DAMAGE_ARMOR] Successfully damaged armor with amount: " + damageAmount);
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

    private int parseAmountFromString(@NotNull String fullAction) {
        if (!fullAction.contains(":")) {
            return 1;
        }

        String[] parts = fullAction.split(":");
        if (parts.length < 2) {
            return 1;
        }

        String amountStr = parts[1].trim();

        // Range támogatás (pl: "3-5")
        if (amountStr.contains("-")) {
            String[] range = amountStr.split("-");
            try {
                int min = Integer.parseInt(range[0].trim());
                int max = Integer.parseInt(range[1].trim());
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException e) {
                LoggerUtil.warn("[DAMAGE_ARMOR] Failed to parse range: " + amountStr);
                return 1;
            }
        }

        // Fix érték
        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            LoggerUtil.warn("[DAMAGE_ARMOR] Failed to parse amount: " + amountStr);
            return 1;
        }
    }
}