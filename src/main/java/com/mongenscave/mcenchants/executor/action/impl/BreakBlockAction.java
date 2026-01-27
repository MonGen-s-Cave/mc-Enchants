package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BreakBlockAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Block centerBlock = (Block) context.get("block");
        if (centerBlock == null) return;

        int radius = actionData.radius();
        if (radius <= 0) radius = 1;

        Location center = centerBlock.getLocation();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getType() == Material.AIR || block.getType() == Material.BEDROCK) {
                        continue;
                    }

                    if (block.isLiquid()) {
                        continue;
                    }

                    if (block.getState() instanceof Container ||
                            block.getState() instanceof Sign) {
                        continue;
                    }

                    block.breakNaturally(player.getInventory().getItemInMainHand());
                }
            }
        }
    }

    @Override
    public String getActionType() {
        return "BREAK_BLOCK";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        return context.containsKey("block");
    }
}