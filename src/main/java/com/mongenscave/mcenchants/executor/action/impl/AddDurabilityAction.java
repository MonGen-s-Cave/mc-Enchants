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
        LoggerUtil.info("ADD_DURABILITY: Starting execution");

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            LoggerUtil.warn("ADD_DURABILITY: Item is air");
            return;
        }

        int durabilityToAdd = (int) actionData.multiplier();
        LoggerUtil.info("ADD_DURABILITY: Adding " + durabilityToAdd + " durability");

        item.editMeta(meta -> {
            if (meta instanceof Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int newDamage = Math.max(0, currentDamage + durabilityToAdd);
                damageable.setDamage(newDamage);
                LoggerUtil.info("ADD_DURABILITY: Damage changed from " + currentDamage + " to " + newDamage);
            }
        });
    }

    @Override
    public String getActionType() {
        return "ADD_DURABILITY_ITEM";
    }
}