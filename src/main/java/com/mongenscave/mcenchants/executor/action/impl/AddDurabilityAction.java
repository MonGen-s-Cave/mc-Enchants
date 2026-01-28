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

public class AddDurabilityAction extends EnchantAction {

    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            return;
        }

        double repairPercent = actionData.multiplier();

        item.editMeta(meta -> {
            if (meta instanceof Damageable damageable) {
                int maxDurability = item.getType().getMaxDurability();
                int currentDamage = damageable.getDamage();

                int durabilityToRestore = (int) (maxDurability * (repairPercent / 100.0));
                int newDamage = Math.max(0, currentDamage - durabilityToRestore);

                damageable.setDamage(newDamage);
            }
        });
    }

    @Override
    public String getActionType() {
        return "ADD_DURABILITY_ITEM";
    }
}