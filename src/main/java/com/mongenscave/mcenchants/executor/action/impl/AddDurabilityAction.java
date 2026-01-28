// ============================================
// KRITIKUS JAVÍTÁS - AddDurabilityAction.java
// RESTORE ENCHANT FIX
// ============================================

package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AddDurabilityAction extends EnchantAction {

    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        LoggerUtil.info("[ADD_DURABILITY] Starting execution");

        Object brokenItemObj = context.get("broken_item");
        ItemStack item = null;

        if (brokenItemObj instanceof ItemStack) {
            item = (ItemStack) brokenItemObj;
            LoggerUtil.info("[ADD_DURABILITY] Got broken_item from context: " + item.getType());
        } else {
            item = player.getInventory().getItemInMainHand();
            LoggerUtil.info("[ADD_DURABILITY] Using main hand item: " + item.getType());
        }

        if (item.getType() == Material.AIR) {
            LoggerUtil.warn("[ADD_DURABILITY] Item is null or AIR");
            return;
        }

        double repairPercent = actionData.multiplier();
        LoggerUtil.info("[ADD_DURABILITY] Repair percent: " + repairPercent + "%");

        Object eventObj = context.get("event");
        if (!(eventObj instanceof PlayerItemDamageEvent damageEvent)) {
            LoggerUtil.warn("[ADD_DURABILITY] Event is not PlayerItemDamageEvent: " + (eventObj != null ? eventObj.getClass().getName() : "null"));
            return;
        }

        damageEvent.setCancelled(true);
        LoggerUtil.info("[ADD_DURABILITY] Cancelled damage event");

        if (!(item.getItemMeta() instanceof Damageable)) {
            LoggerUtil.warn("[ADD_DURABILITY] Item is not damageable");
            return;
        }

        ItemStack finalItem = item;
        item.editMeta(meta -> {
            if (meta instanceof Damageable damageable) {
                int maxDurability = finalItem.getType().getMaxDurability();
                int currentDamage = damageable.getDamage();

                LoggerUtil.info("[ADD_DURABILITY] Before: damage=" + currentDamage + ", max=" + maxDurability);

                int durabilityToRestore = (int) (maxDurability * (repairPercent / 100.0));
                int newDamage = Math.max(0, currentDamage - durabilityToRestore);

                damageable.setDamage(newDamage);

                LoggerUtil.info("[ADD_DURABILITY] After: damage=" + newDamage + ", restored=" + durabilityToRestore);
            }
        });

        LoggerUtil.info("[ADD_DURABILITY] Successfully repaired item");
    }

    @Override
    public String getActionType() {
        return "ADD_DURABILITY_ITEM";
    }
}