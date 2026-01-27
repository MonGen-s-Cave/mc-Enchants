package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlantSeedsAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        LoggerUtil.info("PLANT_SEEDS: Starting execution (tilling dirt to farmland)");

        Block centerBlock = player.getTargetBlockExact(5);
        if (centerBlock == null) {
            LoggerUtil.warn("PLANT_SEEDS: Target block is null");
            return;
        }

        LoggerUtil.info("PLANT_SEEDS: Center block type: " + centerBlock.getType());

        if (!isTillable(centerBlock)) {
            LoggerUtil.warn("PLANT_SEEDS: Center block is not tillable: " + centerBlock.getType());
            return;
        }

        int radius = actionData.radius();
        Location center = centerBlock.getLocation();
        int tilled = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.clone().add(x, 0, z).getBlock();

                if (isTillable(block)) {
                    block.setType(Material.FARMLAND);
                    tilled++;
                }
            }
        }

        LoggerUtil.info("PLANT_SEEDS: Tilled " + tilled + " blocks to farmland");

        if (tilled > 0) {
            player.playSound(player.getLocation(), Sound.ITEM_HOE_TILL, 1.0f, 1.0f);
        }
    }

    @Override
    public String getActionType() {
        return "PLANT_SEEDS";
    }

    private boolean isTillable(@NotNull Block block) {
        Material type = block.getType();
        return type == Material.DIRT ||
                type == Material.GRASS_BLOCK ||
                type == Material.DIRT_PATH;
    }
}