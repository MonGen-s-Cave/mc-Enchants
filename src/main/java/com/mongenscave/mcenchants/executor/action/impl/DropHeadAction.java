package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class DropHeadAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Entity victim = (Entity) context.get("victim");
        if (victim == null) return;

        ItemStack head;

        if (victim instanceof Player playerVictim) {
            head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(playerVictim);
                head.setItemMeta(meta);
            }
        } else {
            head = getMobHead(victim.getType());
        }

        if (head != null) {
            victim.getWorld().dropItemNaturally(victim.getLocation(), head);
        }
    }

    @Override
    public String getActionType() {
        return "DROP_HEAD";
    }

    private @Nullable ItemStack getMobHead(@NotNull EntityType type) {
        return switch (type) {
            case ZOMBIE -> new ItemStack(Material.ZOMBIE_HEAD);
            case SKELETON -> new ItemStack(Material.SKELETON_SKULL);
            case CREEPER -> new ItemStack(Material.CREEPER_HEAD);
            case WITHER_SKELETON -> new ItemStack(Material.WITHER_SKELETON_SKULL);
            case ENDER_DRAGON -> new ItemStack(Material.DRAGON_HEAD);
            case PIGLIN -> new ItemStack(Material.PIGLIN_HEAD);
            default -> null;
        };
    }
}