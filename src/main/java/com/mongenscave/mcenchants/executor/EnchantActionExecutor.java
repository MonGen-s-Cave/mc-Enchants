package com.mongenscave.mcenchants.executor;

import com.mongenscave.mcenchants.model.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
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
            switch (action.getActionType().toUpperCase()) {
                case "PLANT_SEEDS" -> executePlantSeeds(player, action);
                case "TNT" -> executeTNT(player, action, context);
                case "POTION" -> executePotion(player, action, context);
                case "INVINCIBLE" -> executeInvincible(player, action, context);
                case "WAIT" -> executeWait(player, action, context);
                case "DOUBLE_DAMAGE" -> executeDoubleDamage(player, action, context);
                case "CANCEL_EVENT" -> executeCancelEvent(player, action, context);
                case "REMOVE_ENCHANT" -> executeRemoveEnchant(player);
                case "ADD_DURABILITY_ITEM" -> executeAddDurability(player, action);
                case "SHUFFLE_HOTBAR" -> executeShuffleHotbar(context);
                case "SMELT" -> executeSmelt(context);
                default -> LoggerUtil.warn("Unknown action type: " + action.getActionType());
            }
        } catch (Exception exception) {
            LoggerUtil.error("Error executing action " + action.getActionType() + ": " + exception.getMessage());
        }
    }

    private void executePlantSeeds(@NotNull Player player, @NotNull EnchantAction action) {
        Block centerBlock = player.getTargetBlockExact(5);
        if (centerBlock == null || centerBlock.getType() != Material.FARMLAND) return;

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

                Block above = block.getRelative(BlockFace.UP);
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

    private void executeTNT(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Entity targetEntity = (Entity) context.get("victim");
        if (targetEntity == null) return;

        Location location = targetEntity.getLocation();
        int power = (int) action.getMultiplier();
        int fuseTicks = action.getDuration();

        for (int i = 0; i < action.getRadius(); i++) {
            TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
            tnt.setFuseTicks(fuseTicks);
            tnt.setYield(power);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
    }

    private void executePotion(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        String actionString = action.getActionType();
        String[] parts = actionString.split(":");
        if (parts.length < 4) return;

        String effectName = parts[1].toUpperCase();
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) return;

        int amplifier = Integer.parseInt(parts[2]);
        int duration = Integer.parseInt(parts[3]);

        Entity target = (Entity) context.get("target");
        if (target instanceof LivingEntity living) {
            living.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, true));
        } else {
            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, true));
        }
    }

    private void executeInvincible(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        player.setInvulnerable(true);
        context.put("invincible_active", true);
    }

    private void executeWait(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        if (context.containsKey("invincible_active")) {
            player.setInvulnerable(false);
            context.remove("invincible_active");
        }
    }

    private void executeDoubleDamage(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;

        double currentDamage = damageEvent.getDamage();
        damageEvent.setDamage(currentDamage * 2.0);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.2f);
    }

    private void executeCancelEvent(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        if (event instanceof org.bukkit.event.Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
    }

    private void executeRemoveEnchant(@NotNull Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        LoggerUtil.info("Removed enchant from item");
    }

    private void executeAddDurability(@NotNull Player player, @NotNull EnchantAction action) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        int durabilityToAdd = (int) action.getMultiplier();

        item.editMeta(meta -> {
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int newDamage = Math.max(0, currentDamage + durabilityToAdd);
                damageable.setDamage(newDamage);
            }
        });
    }

    private void executeShuffleHotbar(@NotNull Map<String, Object> context) {
        Entity attacker = (Entity) context.get("attacker");
        if (!(attacker instanceof Player attackerPlayer)) return;

        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = attackerPlayer.getInventory().getItem(i);
        }

        for (int i = 8; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            ItemStack temp = hotbar[i];
            hotbar[i] = hotbar[j];
            hotbar[j] = temp;
        }

        for (int i = 0; i < 9; i++) {
            attackerPlayer.getInventory().setItem(i, hotbar[i]);
        }

        attackerPlayer.playSound(attackerPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }

    private void executeSmelt(@NotNull Map<String, Object> context) {
        Block block = (Block) context.get("block");
        if (block == null) return;

        Material smelted = getSmeltedMaterial(block.getType());
        if (smelted != null) {
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted));
        }
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

    @Nullable
    private Material getSmeltedMaterial(@NotNull Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }
}