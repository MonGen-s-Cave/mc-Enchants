package com.mongenscave.mcenchants.listener;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.EnchantActionExecutor;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.executor.condition.ConditionEvaluator;
import com.mongenscave.mcenchants.identifier.EnchantType;
import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantLevel;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("deprecation")
public final class EnchantTriggerListener implements Listener {
    private final EnchantManager enchantManager;
    private final EnchantActionExecutor actionExecutor;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> activeEffects = new ConcurrentHashMap<>();
    private final Map<UUID, ProjectileHitData> recentProjectileHits = new ConcurrentHashMap<>();
    private final Set<String> blacklistedWorlds = new HashSet<>();
    private MyScheduledTask passiveCheckTask;

    private static class ProjectileHitData {
        final LivingEntity target;
        final boolean isHeadshot;
        final long timestamp;

        ProjectileHitData(LivingEntity target, boolean isHeadshot) {
            this.target = target;
            this.isHeadshot = isHeadshot;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public EnchantTriggerListener() {
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.actionExecutor = new EnchantActionExecutor();

        loadBlacklistedWorlds();
        startPassiveEnchantChecker();
    }

    private void loadBlacklistedWorlds() {
        blacklistedWorlds.clear();
        List<String> worlds = ConfigKey.BLACKLISTED_WORLDS.getList();
        blacklistedWorlds.addAll(worlds);
    }

    private boolean isWorldBlacklisted(@NotNull Player player) {
        return blacklistedWorlds.contains(player.getWorld().getName());
    }

    private void startPassiveEnchantChecker() {
        passiveCheckTask = McEnchants.getInstance().getScheduler().runTaskTimer(() -> {
            for (Player player : McEnchants.getInstance().getServer().getOnlinePlayers()) {
                if (!isWorldBlacklisted(player)) {
                    checkAndApplyPassiveEnchants(player);
                }
            }
        }, 20L, 20L);
    }

    private void checkAndApplyPassiveEnchants(@NotNull Player player) {
        Set<String> currentEffects = new HashSet<>();

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;

            Map<String, Integer> enchants = getEnchantsFromItem(piece);
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                String enchantId = entry.getKey();
                int level = entry.getValue();

                Enchant enchant = enchantManager.getEnchant(enchantId);
                if (enchant == null) continue;

                if (enchant.getType() != EnchantType.PASSIVE &&
                        enchant.getType() != EnchantType.EFFECT_STATIC) continue;

                EnchantLevel enchantLevel = enchant.getLevel(level);
                if (enchantLevel == null) continue;

                currentEffects.add(enchantId);

                if (enchant.getType() == EnchantType.EFFECT_STATIC) {
                    applyEffectEnchant(player, enchantLevel);
                }
            }
        }

        Set<String> previousEffects = activeEffects.getOrDefault(player.getUniqueId(), new HashSet<>());

        for (String oldEffect : previousEffects) {
            if (!currentEffects.contains(oldEffect)) {
                removeEffectEnchant(player, oldEffect);
            }
        }

        activeEffects.put(player.getUniqueId(), currentEffects);
    }

    private void applyEffectEnchant(@NotNull Player player, @NotNull EnchantLevel level) {
        level.getActions().forEach(action -> {
            String actionType = action.getActionType();

            if (actionType.toUpperCase().startsWith("POTION:")) {
                String[] parts = actionType.split(":");
                if (parts.length >= 4) {
                    try {
                        String effectName = parts[1].toUpperCase();
                        int amplifier = Integer.parseInt(parts[2]);
                        int duration = Integer.parseInt(parts[3]);

                        PotionEffectType effectType = PotionEffectType.getByName(effectName);
                        if (effectType != null) {
                            PotionEffect existing = player.getPotionEffect(effectType);

                            if (existing == null || existing.getDuration() < 40) {
                                player.addPotionEffect(new PotionEffect(
                                        effectType,
                                        duration,
                                        amplifier,
                                        false,
                                        false
                                ));
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        });
    }

    private void removeEffectEnchant(@NotNull Player player, @NotNull String enchantId) {
        Enchant enchant = enchantManager.getEnchant(enchantId);
        if (enchant == null || enchant.getType() != EnchantType.EFFECT_STATIC) return;

        EnchantLevel level = enchant.getLevel(1);
        if (level == null) return;

        level.getActions().forEach(action -> {
            String actionType = action.getActionType();
            if (actionType.toUpperCase().startsWith("POTION:")) {
                String[] parts = actionType.split(":");
                if (parts.length >= 2) {
                    String effectName = parts[1].toUpperCase();
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null) {
                        player.removePotionEffect(effectType);
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (!isWorldBlacklisted(event.getPlayer())) {
            checkAndApplyPassiveEnchants(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        activeEffects.remove(event.getPlayer().getUniqueId());
        recentProjectileHits.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onItemHeld(@NotNull PlayerItemHeldEvent event) {
        if (!isWorldBlacklisted(event.getPlayer())) {
            McEnchants.getInstance().getScheduler().runTaskLater(() -> checkAndApplyPassiveEnchants(event.getPlayer()), 1L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (isWorldBlacklisted(event.getPlayer())) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        EnchantType type = null;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            type = EnchantType.RIGHT_CLICK;
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            type = EnchantType.LEFT_CLICK;
        }

        if (type != null) {
            Map<String, Object> context = new HashMap<>();
            context.put("player", player);
            context.put("target", player);
            if (event.getClickedBlock() != null) {
                context.put("block", event.getClickedBlock());
            }
            context.put("event", event);

            triggerEnchants(player, item, type, context);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (isWorldBlacklisted(event.getPlayer())) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        context.put("block", event.getBlock());
        context.put("target", player);
        context.put("event", event);

        triggerEnchants(player, item, EnchantType.BREAK_BLOCK, context);
        triggerEnchants(player, item, EnchantType.MINING, context);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (isWorldBlacklisted(attacker)) return;

            ItemStack item = attacker.getInventory().getItemInMainHand();

            Map<String, Object> context = new HashMap<>();
            context.put("player", attacker);
            context.put("attacker", attacker);
            context.put("victim", event.getEntity());
            context.put("target", event.getEntity());
            context.put("event", event);
            context.put("damage", event.getDamage());

            triggerEnchants(attacker, item, EnchantType.ATTACK, context);

            if (!(event.getEntity() instanceof Player)) triggerEnchants(attacker, item, EnchantType.ATTACK_MOB, context);
        }

        if (event.getEntity() instanceof Player victim) {
            if (isWorldBlacklisted(victim)) return;

            Map<String, Object> context = new HashMap<>();
            context.put("player", victim);
            context.put("victim", victim);
            context.put("attacker", event.getDamager());
            context.put("target", victim);
            context.put("event", event);
            context.put("damage", event.getDamage());

            ItemStack[] armor = victim.getInventory().getArmorContents();
            for (ItemStack piece : armor) {
                if (piece != null && !piece.getType().isAir()) triggerEnchants(victim, piece, EnchantType.DEFENSE, context);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isWorldBlacklisted(player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null && !boots.getType().isAir()) {
                Map<String, Object> context = new HashMap<>();
                context.put("player", player);
                context.put("target", player);
                context.put("event", event);
                context.put("damage", event.getDamage());

                triggerEnchants(player, boots, EnchantType.FALL_DAMAGE, context);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityShootBow(@NotNull EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isWorldBlacklisted(player)) return;

        ItemStack item = event.getBow();
        if (item == null) return;

        if (event.getProjectile() instanceof Projectile projectile) {
            projectile.getPersistentDataContainer().set(
                    new NamespacedKey(McEnchants.getInstance(), "shooter"),
                    PersistentDataType.STRING,
                    player.getUniqueId().toString()
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(@NotNull ProjectileHitEvent event) {
        if (event.getHitEntity() == null) return;
        if (!(event.getHitEntity() instanceof LivingEntity target)) return;

        Projectile projectile = event.getEntity();

        PersistentDataContainer pdc = projectile.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "shooter");

        if (!pdc.has(key, PersistentDataType.STRING)) return;

        String shooterUUID = pdc.get(key, PersistentDataType.STRING);
        if (shooterUUID == null) return;

        Player shooter = McEnchants.getInstance().getServer().getPlayer(UUID.fromString(shooterUUID));
        if (shooter == null) return;
        if (isWorldBlacklisted(shooter)) return;

        Location hitLocation = projectile.getLocation();
        Location eyeLocation = target.getEyeLocation();
        boolean isHeadshot = Math.abs(hitLocation.getY() - eyeLocation.getY()) < 0.5;

        recentProjectileHits.put(shooter.getUniqueId(), new ProjectileHitData(target, isHeadshot));

        ItemStack bow = shooter.getInventory().getItemInMainHand();
        if (!bow.getType().name().contains("BOW") && !bow.getType().name().contains("CROSSBOW")) {
            bow = shooter.getInventory().getItemInOffHand();
        }

        Map<String, Object> context = new HashMap<>();
        context.put("player", shooter);
        context.put("attacker", shooter);
        context.put("victim", target);
        context.put("target", target);
        context.put("projectile", projectile);
        context.put("headshot", isHeadshot);
        context.put("event", event);

        triggerEnchants(shooter, bow, EnchantType.SHOOT, context);

        McEnchants.getInstance().getScheduler().runTaskLater(() -> recentProjectileHits.remove(shooter.getUniqueId()), 20L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;
        if (isWorldBlacklisted(killer)) return;

        ItemStack item = killer.getInventory().getItemInMainHand();

        Map<String, Object> context = new HashMap<>();
        context.put("player", killer);
        context.put("attacker", killer);
        context.put("victim", victim);
        context.put("target", victim);
        context.put("killer", killer);
        context.put("event", event);

        triggerEnchants(killer, item, EnchantType.KILL_PLAYER, context);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Player) return;

        Player killer = entity.getKiller();
        if (killer == null) return;
        if (isWorldBlacklisted(killer)) return;

        ItemStack item = killer.getInventory().getItemInMainHand();

        int exp = event.getDroppedExp();

        Map<String, Object> context = new HashMap<>();
        context.put("player", killer);
        context.put("attacker", killer);
        context.put("victim", entity);
        context.put("target", entity);
        context.put("exp", exp);
        context.put("event", event);

        triggerEnchants(killer, item, EnchantType.KILL_MOB, context);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(@NotNull PlayerItemDamageEvent event) {
        if (isWorldBlacklisted(event.getPlayer())) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.getType().isAir()) return;

        if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable meta)) return;

        int currentDamage = meta.getDamage();
        int maxDurability = item.getType().getMaxDurability();
        int damage = event.getDamage();

        LoggerUtil.info("[ITEM_BREAK] Item: " + item.getType() + ", Current damage: " + currentDamage + ", Max: " + maxDurability + ", Incoming: " + damage);

        if (currentDamage + damage >= maxDurability) {
            LoggerUtil.info("[ITEM_BREAK] Item will break! Triggering ITEM_BREAK enchants");

            Map<String, Object> context = new HashMap<>();
            context.put("player", player);
            context.put("target", player);
            context.put("broken_item", item);
            context.put("event", event);

            triggerEnchants(player, item, EnchantType.ITEM_BREAK, context);
        }
    }

    private void triggerEnchants(@NotNull Player player, @Nullable ItemStack item, @NotNull EnchantType type,
                                 @NotNull Map<String, Object> context) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        Map<String, Integer> enchants = getEnchantsFromItem(item);
        if (enchants.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            String enchantId = entry.getKey();
            int level = entry.getValue();

            Enchant enchant = enchantManager.getEnchant(enchantId);
            if (enchant == null) {
                continue;
            }

            if (enchant.getType() != type) {
                continue;
            }

            EnchantLevel enchantLevel = enchant.getLevel(level);
            if (enchantLevel == null) {
                continue;
            }

            int chance = enchantLevel.getChance();
            if (chance < 100) {
                int roll = ThreadLocalRandom.current().nextInt(1, 101);
                if (roll > chance) {
                    continue;
                }
            }

            String cooldownKey = player.getUniqueId() + ":" + enchantId;
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(cooldownKey);

            if (lastUse != null && (now - lastUse) < enchantLevel.getCooldown() * 1000L) {
                continue;
            }

            if (!checkConditions(enchantLevel, context, player)) {
                continue;
            }

            executeActions(player, enchantLevel, context);

            if (enchantLevel.getCooldown() > 0) {
                cooldowns.put(cooldownKey, now);
            }
        }
    }

    @NotNull
    private Map<String, Integer> getEnchantsFromItem(@NotNull ItemStack item) {
        Map<String, Integer> enchants = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return enchants;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) {
            return enchants;
        }

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) {
            return enchants;
        }

        String[] entries = enchantsData.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    String enchantId = parts[0].trim();
                    int level = Integer.parseInt(parts[1].trim());
                    enchants.put(enchantId, level);
                } catch (NumberFormatException exception) {
                    LoggerUtil.error(exception.getMessage());
                }
            }
        }

        return enchants;
    }

    private boolean checkConditions(@NotNull EnchantLevel level, @NotNull Map<String, Object> context, @NotNull Player player) {
        if (level.getConditions().isEmpty()) {
            return true;
        }

        for (String condition : level.getConditions()) {
            if (!ConditionEvaluator.evaluateCondition(condition, context, player)) {
                return false;
            }
        }

        return true;
    }

    private void executeActions(@NotNull Player player, @NotNull EnchantLevel level, @NotNull Map<String, Object> context) {
        level.getActions().forEach(modelAction -> {
            EnchantAction executorAction =
                    new EnchantAction() {
                        @Override
                        public void execute(@NotNull Player p, @NotNull ActionData actionData, @NotNull Map<String, Object> ctx) {}

                        @Contract(pure = true)
                        @Override
                        public @NonNull String getActionType() {
                            return modelAction.getActionType();
                        }
                    };

            actionExecutor.executeAction(player, executorAction, context);
        });
    }

    public void shutdown() {
        if (passiveCheckTask != null) passiveCheckTask.cancel();
        activeEffects.clear();
        cooldowns.clear();
        recentProjectileHits.clear();
    }

    public void reloadBlacklistedWorlds() {
        loadBlacklistedWorlds();
    }
}