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
        Object brokenItemObj = context.get("broken_item");
        ItemStack item = null;

        if (brokenItemObj instanceof ItemStack) {
            item = (ItemStack) brokenItemObj;
        } else {
            item = player.getInventory().getItemInMainHand();
        }

        if (item.getType() == Material.AIR) {
            LoggerUtil.warn("[ADD_DURABILITY] Item is null or AIR");
            return;
        }

        // A multiplier értéke a százalék (pl. 50.0 = 50%)
        double repairPercent = actionData.multiplier();

        Object eventObj = context.get("event");
        if (!(eventObj instanceof PlayerItemDamageEvent damageEvent)) {
            LoggerUtil.warn("[ADD_DURABILITY] Event is not PlayerItemDamageEvent");
            return;
        }

        // Megakadályozzuk a durability csökkenést
        damageEvent.setCancelled(true);

        if (!(item.getItemMeta() instanceof Damageable)) {
            LoggerUtil.warn("[ADD_DURABILITY] Item is not damageable");
            return;
        }

        ItemStack finalItem = item;
        item.editMeta(meta -> {
            if (meta instanceof Damageable damageable) {
                int maxDurability = finalItem.getType().getMaxDurability();
                int currentDamage = damageable.getDamage();

                // Számítsuk ki mennyit javítunk (a max durability %-a)
                int durabilityToRestore = (int) Math.ceil(maxDurability * (repairPercent / 100.0));

                // Az új damage érték (csökkentjük a damage-t = javítás)
                int newDamage = Math.max(0, currentDamage - durabilityToRestore);

                damageable.setDamage(newDamage);

                LoggerUtil.info("[ADD_DURABILITY] Repaired item: " + finalItem.getType() +
                        " | Before damage: " + currentDamage +
                        " | After damage: " + newDamage +
                        " | Restored: " + durabilityToRestore +
                        " | Max durability: " + maxDurability +
                        " | Repair %: " + repairPercent);
            }
        });
    }

    @Override
    public String getActionType() {
        return "ADD_DURABILITY_ITEM";
    }
}