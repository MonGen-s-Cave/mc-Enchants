package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlantSeedsAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Block centerBlock = player.getTargetBlockExact(5);

        if (centerBlock == null || centerBlock.getType() == Material.AIR) return;
        if (!isTillable(centerBlock)) return;

        int radius = actionData.radius();
        Location center = centerBlock.getLocation();
        int tilled = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.clone().add(x, 0, z).getBlock();

                if (isTillable(block)) {
                    Block above = block.getRelative(BlockFace.UP);
                    if (above.getType() == Material.AIR) {
                        block.setType(Material.FARMLAND);
                        tilled++;
                    }
                }
            }
        }

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
                type == Material.DIRT_PATH ||
                type == Material.COARSE_DIRT;
    }
}