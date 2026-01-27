package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RemoveEnchantAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");
        if (parts.length < 2) return;

        String enchantIdToRemove = parts[1];

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;

        removeEnchantFromItem(item, enchantIdToRemove);
    }

    @Override
    public String getActionType() {
        return "REMOVE_ENCHANT";
    }

    private void removeEnchantFromItem(@NotNull ItemStack item, @NotNull String enchantIdToRemove) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) return;

        String[] enchantEntries = enchantsData.split(";");
        StringBuilder newEnchantsData = new StringBuilder();

        for (String entry : enchantEntries) {
            String[] enchantParts = entry.split(":");
            if (enchantParts.length == 2) {
                String enchantId = enchantParts[0].trim();

                if (!enchantId.equals(enchantIdToRemove)) {
                    if (!newEnchantsData.isEmpty()) {
                        newEnchantsData.append(";");
                    }
                    newEnchantsData.append(entry);
                }
            }
        }

        if (!newEnchantsData.isEmpty()) {
            pdc.set(key, PersistentDataType.STRING, newEnchantsData.toString());
        } else {
            pdc.remove(key);
        }

        item.setItemMeta(meta);
    }
}