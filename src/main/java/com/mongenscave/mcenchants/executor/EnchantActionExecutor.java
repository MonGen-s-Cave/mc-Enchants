package com.mongenscave.mcenchants.executor;

import com.mongenscave.mcenchants.model.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

// undorito ES meg nem is megy -,-
public class EnchantActionExecutor {

    public void executeAction(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        try {
            String actionType = action.getActionType();
            String baseType = actionType.split(":")[0].toUpperCase();

            LoggerUtil.info("Executing action - Full: " + actionType + ", Base: " + baseType);

            switch (baseType) {
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
                default -> LoggerUtil.warn("Unknown action type: " + baseType);
            }
        } catch (Exception exception) {
            LoggerUtil.error("Error executing action " + action.getActionType() + ": " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void executePlantSeeds(@NotNull Player player, @NotNull EnchantAction action) {
        LoggerUtil.info("PLANT_SEEDS: Starting execution (tilling dirt to farmland)");

        Block centerBlock = player.getTargetBlockExact(5);
        if (centerBlock == null) {
            LoggerUtil.warn("PLANT_SEEDS: Target block is null");
            return;
        }

        LoggerUtil.info("PLANT_SEEDS: Center block type: " + centerBlock.getType());

        if (centerBlock.getType() != Material.DIRT &&
                centerBlock.getType() != Material.GRASS_BLOCK &&
                centerBlock.getType() != Material.DIRT_PATH) {
            LoggerUtil.warn("PLANT_SEEDS: Center block is not tillable: " + centerBlock.getType());
            return;
        }

        int radius = action.getRadius();
        Location center = centerBlock.getLocation();
        int tilled = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.clone().add(x, 0, z).getBlock();

                if (block.getType() == Material.DIRT ||
                        block.getType() == Material.GRASS_BLOCK ||
                        block.getType() == Material.DIRT_PATH) {

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

    private void executeTNT(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        LoggerUtil.info("TNT: Starting execution");

        Entity targetEntity = (Entity) context.get("victim");
        if (targetEntity == null) {
            LoggerUtil.warn("TNT: No victim entity in context");
            return;
        }

        Location location = targetEntity.getLocation();
        int power = (int) action.getMultiplier();
        int fuseTicks = action.getDuration();

        LoggerUtil.info("TNT: Spawning " + action.getRadius() + " TNT with power " + power);

        for (int i = 0; i < action.getRadius(); i++) {
            TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
            tnt.setFuseTicks(fuseTicks);
            tnt.setYield(power);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
    }

    private void executePotion(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        String actionString = action.getActionType();
        LoggerUtil.info("POTION: Parsing action string: " + actionString);

        String[] parts = actionString.split(":");
        if (parts.length < 4) {
            LoggerUtil.warn("POTION: Invalid format. Expected POTION:EFFECT:LEVEL:DURATION, got: " + actionString);
            return;
        }

        String effectName = parts[1].toUpperCase();
        LoggerUtil.info("POTION: Effect name: " + effectName);

        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) {
            LoggerUtil.warn("POTION: Unknown effect type: " + effectName);
            return;
        }

        int amplifier = Integer.parseInt(parts[2]);
        int duration = Integer.parseInt(parts[3]);

        LoggerUtil.info("POTION: Applying " + effectName + " level " + amplifier + " for " + duration + " ticks to player");

        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, false, true));
        LoggerUtil.info("POTION: Successfully applied effect to player");
    }

    private void executeInvincible(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        LoggerUtil.info("INVINCIBLE: Setting player invulnerable");
        player.setInvulnerable(true);
        context.put("invincible_active", true);
    }

    private void executeWait(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        LoggerUtil.info("WAIT: Checking invincibility status");
        if (context.containsKey("invincible_active")) {
            player.setInvulnerable(false);
            context.remove("invincible_active");
            LoggerUtil.info("WAIT: Removed invincibility");
        }
    }

    private void executeDoubleDamage(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        LoggerUtil.info("DOUBLE_DAMAGE: Starting execution");

        Event event = (Event) context.get("event");
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) {
            LoggerUtil.warn("DOUBLE_DAMAGE: Event is not EntityDamageByEntityEvent");
            return;
        }

        double currentDamage = damageEvent.getDamage();
        damageEvent.setDamage(currentDamage * 2.0);

        LoggerUtil.info("DOUBLE_DAMAGE: Damage doubled from " + currentDamage + " to " + (currentDamage * 2.0));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.2f);
    }

    private void executeCancelEvent(@NotNull Player player, @NotNull EnchantAction action, @NotNull Map<String, Object> context) {
        LoggerUtil.info("CANCEL_EVENT: Starting execution");

        Event event = (Event) context.get("event");
        if (event instanceof org.bukkit.event.Cancellable cancellable) {
            cancellable.setCancelled(true);
            LoggerUtil.info("CANCEL_EVENT: Event cancelled");
        } else {
            LoggerUtil.warn("CANCEL_EVENT: Event is not cancellable");
        }
    }

    private void executeRemoveEnchant(@NotNull Player player) {
        LoggerUtil.info("REMOVE_ENCHANT: Starting execution");

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            LoggerUtil.warn("REMOVE_ENCHANT: Item is air");
            return;
        }

        LoggerUtil.info("REMOVE_ENCHANT: Removed enchant from item");
    }

    private void executeAddDurability(@NotNull Player player, @NotNull EnchantAction action) {
        LoggerUtil.info("ADD_DURABILITY: Starting execution");

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            LoggerUtil.warn("ADD_DURABILITY: Item is air");
            return;
        }

        int durabilityToAdd = (int) action.getMultiplier();
        LoggerUtil.info("ADD_DURABILITY: Adding " + durabilityToAdd + " durability");

        item.editMeta(meta -> {
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int newDamage = Math.max(0, currentDamage + durabilityToAdd);
                damageable.setDamage(newDamage);
                LoggerUtil.info("ADD_DURABILITY: Damage changed from " + currentDamage + " to " + newDamage);
            }
        });
    }

    private void executeShuffleHotbar(@NotNull Map<String, Object> context) {
        LoggerUtil.info("SHUFFLE_HOTBAR: Starting execution");

        Entity attacker = (Entity) context.get("attacker");
        if (!(attacker instanceof Player attackerPlayer)) {
            LoggerUtil.warn("SHUFFLE_HOTBAR: Attacker is not a player");
            return;
        }

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
        LoggerUtil.info("SHUFFLE_HOTBAR: Hotbar shuffled");
    }

    private void executeSmelt(@NotNull Map<String, Object> context) {
        LoggerUtil.info("SMELT: Starting execution");

        Block block = (Block) context.get("block");
        if (block == null) {
            LoggerUtil.warn("SMELT: No block in context");
            return;
        }

        Material smelted = getSmeltedMaterial(block.getType());
        if (smelted != null) {
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted));
            LoggerUtil.info("SMELT: Smelted " + block.getType() + " to " + smelted);
        } else {
            LoggerUtil.warn("SMELT: No smelted material for " + block.getType());
        }
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