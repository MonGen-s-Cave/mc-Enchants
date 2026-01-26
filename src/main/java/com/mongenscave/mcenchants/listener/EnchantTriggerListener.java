package com.mongenscave.mcenchants.listener;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.executor.EnchantActionExecutor;
import com.mongenscave.mcenchants.identifier.EnchantType;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantLevel;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class EnchantTriggerListener implements Listener {
    private final EnchantManager enchantManager;
    private final EnchantActionExecutor actionExecutor;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private static final boolean DEBUG = true;

    public EnchantTriggerListener() {
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.actionExecutor = new EnchantActionExecutor();

        if (DEBUG) {
            LoggerUtil.info("EnchantTriggerListener initialized!");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        EnchantType type = null;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            type = EnchantType.RIGHT_CLICK;
            if (DEBUG) {
                LoggerUtil.info("RIGHT_CLICK detected for player: " + player.getName());
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            type = EnchantType.LEFT_CLICK;
            if (DEBUG) {
                LoggerUtil.info("LEFT_CLICK detected for player: " + player.getName());
            }
        }

        if (type != null) {
            triggerEnchants(player, item, type, createContext(player, event.getClickedBlock(), null, null));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (DEBUG) {
            LoggerUtil.info("BREAK_BLOCK detected for player: " + player.getName());
        }

        triggerEnchants(player, item, EnchantType.BREAK_BLOCK,
                createContext(player, event.getBlock(), null, null));

        triggerEnchants(player, item, EnchantType.MINING,
                createContext(player, event.getBlock(), null, null));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (DEBUG) {
            LoggerUtil.info("ATTACK detected for player: " + player.getName());
        }

        Map<String, Object> context = createContext(player, null, event.getEntity(), event);
        context.put("victim", event.getEntity());

        triggerEnchants(player, item, EnchantType.ATTACK, context);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event instanceof EntityDamageByEntityEvent damageByEntity) {
            Map<String, Object> context = createContext(player, null, damageByEntity.getDamager(), event);
            context.put("attacker", damageByEntity.getDamager());

            if (DEBUG) {
                LoggerUtil.info("DEFENSE detected for player: " + player.getName());
            }

            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack piece : armor) {
                if (piece != null) {
                    triggerEnchants(player, piece, EnchantType.DEFENSE, context);
                }
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (DEBUG) {
                LoggerUtil.info("FALL_DAMAGE detected for player: " + player.getName());
            }

            ItemStack boots = player.getInventory().getBoots();
            if (boots != null) {
                triggerEnchants(player, boots, EnchantType.FALL_DAMAGE,
                        createContext(player, null, null, event));
            }
        }
    }

    private void triggerEnchants(@NotNull Player player, @Nullable ItemStack item, @NotNull EnchantType type,
                                 @NotNull Map<String, Object> context) {
        if (item == null || item.getType().isAir()) {
            if (DEBUG) {
                LoggerUtil.info("Item is null or air, skipping enchant trigger");
            }
            return;
        }

        Map<String, Integer> enchants = getEnchantsFromItem(item);
        if (enchants.isEmpty()) {
            if (DEBUG) {
                LoggerUtil.info("No enchants found on item: " + item.getType().name());
            }
            return;
        }

        if (DEBUG) {
            LoggerUtil.info("Found " + enchants.size() + " enchant(s) on item: " + enchants.keySet());
        }

        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            String enchantId = entry.getKey();
            int level = entry.getValue();

            if (DEBUG) {
                LoggerUtil.info("Processing enchant: " + enchantId + " level " + level);
            }

            Enchant enchant = enchantManager.getEnchant(enchantId);
            if (enchant == null) {
                if (DEBUG) {
                    LoggerUtil.warn("Enchant not found in manager: " + enchantId);
                }
                continue;
            }

            if (enchant.getType() != type) {
                if (DEBUG) {
                    LoggerUtil.info("Enchant type mismatch: expected " + type + ", got " + enchant.getType());
                }
                continue;
            }

            EnchantLevel enchantLevel = enchant.getLevel(level);
            if (enchantLevel == null) {
                if (DEBUG) {
                    LoggerUtil.warn("Enchant level not found: " + level + " for enchant " + enchantId);
                }
                continue;
            }

            int chance = enchantLevel.getChance();
            if (chance < 100) {
                int roll = ThreadLocalRandom.current().nextInt(1, 101);
                if (DEBUG) {
                    LoggerUtil.info("Chance roll: " + roll + "/" + chance);
                }
                if (roll > chance) {
                    if (DEBUG) {
                        LoggerUtil.info("Chance roll failed, skipping");
                    }
                    continue;
                }
            }

            String cooldownKey = player.getUniqueId() + ":" + enchantId;
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(cooldownKey);

            if (lastUse != null && (now - lastUse) < enchantLevel.getCooldown() * 1000L) {
                if (DEBUG) {
                    long remaining = (enchantLevel.getCooldown() * 1000L - (now - lastUse)) / 1000;
                    LoggerUtil.info("Cooldown active, " + remaining + "s remaining");
                }
                continue;
            }

            if (!checkConditions(enchantLevel, context, player)) {
                if (DEBUG) {
                    LoggerUtil.info("Conditions not met for enchant " + enchantId);
                }
                continue;
            }

            if (DEBUG) {
                LoggerUtil.info("Executing enchant: " + enchantId + " level " + level);
            }

            executeActions(player, enchantLevel, context);

            if (enchantLevel.getCooldown() > 0) {
                cooldowns.put(cooldownKey, now);
                if (DEBUG) {
                    LoggerUtil.info("Cooldown set: " + enchantLevel.getCooldown() + "s");
                }
            }
        }
    }

    @NotNull
    private Map<String, Integer> getEnchantsFromItem(@NotNull ItemStack item) {
        Map<String, Integer> enchants = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            if (DEBUG) {
                LoggerUtil.info("Item meta is null");
            }
            return enchants;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) {
            if (DEBUG) {
                LoggerUtil.info("No 'enchants' PDC key found on item");
            }
            return enchants;
        }

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) {
            if (DEBUG) {
                LoggerUtil.info("Enchants data is null or empty");
            }
            return enchants;
        }

        if (DEBUG) {
            LoggerUtil.info("Raw enchants data: " + enchantsData);
        }

        String[] entries = enchantsData.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    String enchantId = parts[0].trim();
                    int level = Integer.parseInt(parts[1].trim());
                    enchants.put(enchantId, level);
                    if (DEBUG) {
                        LoggerUtil.info("Parsed enchant: " + enchantId + " -> " + level);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        LoggerUtil.warn("Failed to parse enchant entry: " + entry);
                    }
                }
            }
        }

        return enchants;
    }

    private boolean checkConditions(@NotNull EnchantLevel level, @NotNull Map<String, Object> context, @NotNull Player player) {
        if (level.getConditions().isEmpty()) {
            if (DEBUG) {
                LoggerUtil.info("No conditions to check");
            }
            return true;
        }

        for (String condition : level.getConditions()) {
            if (!evaluateCondition(condition, context, player)) {
                if (DEBUG) {
                    LoggerUtil.info("Condition failed: " + condition);
                }
                return false;
            }
        }

        if (DEBUG) {
            LoggerUtil.info("All conditions passed");
        }
        return true;
    }

    private boolean evaluateCondition(@NotNull String condition, @NotNull Map<String, Object> context, @NotNull Player player) {
        String[] parts = condition.split("=>");
        if (parts.length != 2) {
            if (DEBUG) {
                LoggerUtil.warn("Invalid condition format: " + condition);
            }
            return true;
        }

        String check = parts[0].trim();
        String result = parts[1].trim();

        if (DEBUG) {
            LoggerUtil.info("Evaluating condition: " + check + " => " + result);
        }

        if (check.contains("{sneaking}")) {
            boolean sneaking = player.isSneaking();
            boolean expected = check.contains("true");

            if (DEBUG) {
                LoggerUtil.info("Sneaking check: player=" + sneaking + ", expected=" + expected);
            }

            if (sneaking == expected) {
                boolean allowed = result.equalsIgnoreCase("allow");
                if (DEBUG) {
                    LoggerUtil.info("Condition matched, result: " + (allowed ? "ALLOW" : "DENY"));
                }
                return allowed;
            } else {
                if (DEBUG) {
                    LoggerUtil.info("Condition not matched, DENY");
                }
                return false;
            }
        }

        return result.equalsIgnoreCase("allow");
    }

    private void executeActions(@NotNull Player player, @NotNull EnchantLevel level, @NotNull Map<String, Object> context) {
        if (DEBUG) {
            LoggerUtil.info("Executing " + level.getActions().size() + " action(s)");
        }

        level.getActions().forEach(action -> {
            if (DEBUG) {
                LoggerUtil.info("Action: " + action.getActionType());
            }
            actionExecutor.executeAction(player, action, context);
        });
    }

    @NotNull
    private Map<String, Object> createContext(@NotNull Player player, @Nullable Object block, @Nullable Object entity, @Nullable Object event) {
        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        context.put("target", player);
        if (block != null) context.put("block", block);
        if (entity != null) context.put("entity", entity);
        if (event != null) context.put("event", event);
        return context;
    }
}