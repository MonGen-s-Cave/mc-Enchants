package com.mongenscave.mcenchants.executor;

import com.mongenscave.mcenchants.model.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EnchantActionExecutor {

    public void executeAction(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        try {
            switch (action.getType().toUpperCase()) {
                case "PLANT_SEEDS" -> executePlantSeeds(player, action, context);
                case "DAMAGE_BOOST" -> executeDamageBoost(player, action, context);
                case "AREA_BREAK" -> executeAreaBreak(player, action, context);
                case "POTION_EFFECT" -> executePotionEffect(player, action, context);
                case "HEAL" -> executeHeal(player, action, context);
                case "LIGHTNING" -> executeLightning(player, action, context);
                case "EXPLOSION" -> executeExplosion(player, action, context);
                default -> LoggerUtil.warn("Unknown action type: " + action.getType());
            }
        } catch (Exception exception) {
            LoggerUtil.error("Error executing action " + action.getType() + ": " + exception.getMessage());
        }
    }

    private void executePlantSeeds(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Block centerBlock = player.getTargetBlock(null, 5);
        if (centerBlock.getType() != Material.FARMLAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        Material seedType = getSeedType(item.getType());
        if (seedType == null) return;

        int radius = action.getRadius();
        Location center = centerBlock.getLocation();
        int planted = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.clone().add(x, 0, z).getBlock();
                if (block.getType() != Material.FARMLAND) continue;

                Block above = block.getRelative(0, 1, 0);
                if (above.getType() != Material.AIR) continue;

                if (item.getAmount() > 0) {
                    above.setType(seedType);
                    item.setAmount(item.getAmount() - 1);
                    planted++;
                }
            }
        }

        if (planted > 0) {
            player.playSound(player.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
        }
    }

    private void executeDamageBoost(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;

        double currentDamage = damageEvent.getDamage();
        double newDamage = currentDamage * action.getMultiplier();
        damageEvent.setDamage(newDamage);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.2f);
    }

    private void executeAreaBreak(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Block centerBlock = (Block) context.get("block");
        if (centerBlock == null) return;

        Material centerType = centerBlock.getType();
        Location center = centerBlock.getLocation();
        int radius = action.getRadius();
        int broken = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getType() == centerType && !block.equals(centerBlock)) {
                        block.breakNaturally(player.getInventory().getItemInMainHand());
                        broken++;
                    }
                }
            }
        }

        if (broken > 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
        }
    }

    private void executePotionEffect(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        String[] parts = action.getType().split(":");
        if (parts.length < 2) return;

        String effectName = parts[1].toUpperCase();
        PotionEffectType effectType = PotionEffectType.getByName(effectName);

        if (effectType == null) return;

        int amplifier = (int) (action.getMultiplier() - 1);
        int duration = action.getDuration() * 20;

        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, true));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private void executeHeal(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double healAmount = action.getMultiplier();

        double newHealth = Math.min(maxHealth, currentHealth + healAmount);
        player.setHealth(newHealth);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.3f);
    }

    private void executeLightning(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Entity targetEntity = (Entity) context.get("entity");

        if (targetEntity != null) {
            targetEntity.getWorld().strikeLightning(targetEntity.getLocation());
        } else {
            Block targetBlock = player.getTargetBlock(null, 50);
            targetBlock.getWorld().strikeLightning(targetBlock.getLocation());
        }
    }

    private void executeExplosion(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Entity targetEntity = (Entity) context.get("entity");
        Location explosionLocation;

        if (targetEntity != null) {
            explosionLocation = targetEntity.getLocation();
        } else {
            Block targetBlock = player.getTargetBlock(null, 50);
            explosionLocation = targetBlock.getLocation();
        }

        float power = (float) (action.getMultiplier() * 2.0);
        boolean setFire = false;
        boolean breakBlocks = action.getRadius() > 2;

        explosionLocation.getWorld().createExplosion(
                explosionLocation,
                power,
                setFire,
                breakBlocks
        );
    }

    @Nullable
    private Material getSeedType(@NotNull Material heldItem) {
        return switch (heldItem) {
            case WHEAT_SEEDS -> Material.WHEAT;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case MELON_SEEDS -> Material.MELON_STEM;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case CARROT -> Material.CARROTS;
            case POTATO -> Material.POTATOES;
            case SWEET_BERRIES -> Material.SWEET_BERRY_BUSH;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}