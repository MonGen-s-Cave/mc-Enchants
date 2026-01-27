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
        String actionString = actionData.fullActionString();
        String[] parts = actionString.split(":");

        Entity target = null;
        if (parts.length >= 2) {
            String targetSpec = parts[1].toUpperCase();
            if (targetSpec.equals("@VICTIM")) {
                Object victim = context.get("victim");
                if (victim instanceof Entity) {
                    target = (Entity) victim;
                } else {
                    return;
                }
            } else if (targetSpec.equals("@ATTACKER")) {
                Object attacker = context.get("attacker");
                if (attacker instanceof Entity) {
                    target = (Entity) attacker;
                } else {
                    return;
                }
            }
        } else {
            Object victim = context.get("victim");
            if (victim instanceof Entity) {
                target = (Entity) victim;
            } else {
                return;
            }
        }

        if (target == null) return;

        ItemStack head;

        if (target instanceof Player playerVictim) {
            head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(playerVictim);
                head.setItemMeta(meta);
            }
        } else {
            head = getMobHead(target.getType());
        }

        if (head != null) {
            target.getWorld().dropItemNaturally(target.getLocation(), head);
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