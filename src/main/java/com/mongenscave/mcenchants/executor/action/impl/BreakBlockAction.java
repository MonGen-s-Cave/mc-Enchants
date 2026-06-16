package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BreakBlockAction extends EnchantAction {
    private static final Set<Material> IGNORED_MATERIALS = new HashSet<>();
    private static boolean respectProtection = true;

    static {
        loadIgnoredMaterials();
    }

    private static void loadIgnoredMaterials() {
        IGNORED_MATERIALS.clear();

        List<String> ignoredList = ConfigKey.IGNORED_MATERIALS.getList();

        for (String materialName : ignoredList) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                IGNORED_MATERIALS.add(material);
            } catch (IllegalArgumentException ignored) {}
        }

        respectProtection = ConfigKey.RESPECT_PROTECTION.getBoolean();
    }

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

                    if (shouldIgnoreBlock(block)) continue;
                    if (block.equals(centerBlock)) continue;
                    if (!canBreak(player, block)) continue;

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

    private boolean shouldIgnoreBlock(@NotNull Block block) {
        if (block.getType() == Material.AIR) return true;
        if (IGNORED_MATERIALS.contains(block.getType())) return true;
        if (block.isLiquid()) return true;

        return block.getState() instanceof Container || block.getState() instanceof Sign;
    }

    private boolean canBreak(@NotNull Player player, @NotNull Block block) {
        if (!respectProtection) return true;

        BlockBreakEvent event = new BlockBreakEvent(block, player);
        event.setDropItems(false);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    public static void reloadIgnoredMaterials() {
        loadIgnoredMaterials();
    }
}