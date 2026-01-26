package com.mongenscave.mcenchants.listener;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.executor.EnchantActionExecutor;
import com.mongenscave.mcenchants.identifier.EnchantType;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantLevel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

public final class EnchantTriggerListener implements Listener {
    private final EnchantManager enchantManager;
    private final EnchantActionExecutor actionExecutor;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public EnchantTriggerListener() {
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.actionExecutor = new EnchantActionExecutor();
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        EnchantType type = null;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            type = EnchantType.RIGHT_CLICK;
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            type = EnchantType.LEFT_CLICK;
        }

        if (type != null) {
            triggerEnchants(player, item, type, createContext(player, event.getClickedBlock(), null, null));
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        triggerEnchants(player, item, EnchantType.BREAK_BLOCK,
                createContext(player, event.getBlock(), null, null));
    }

    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        triggerEnchants(player, item, EnchantType.DAMAGE_ENTITY,
                createContext(player, null, event.getEntity(), event));
    }

    @EventHandler
    public void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece != null) {
                triggerEnchants(player, piece, EnchantType.TAKE_DAMAGE,
                        createContext(player, null, null, event));
            }
        }
    }

    private void triggerEnchants(@NotNull Player player, @Nullable ItemStack item, @NotNull EnchantType type,
                                 @NotNull Map<String, Object> context) {
        if (item == null) return;

        Map<String, Integer> enchants = getEnchantsFromItem(item);
        if (enchants.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            String enchantId = entry.getKey();
            int level = entry.getValue();

            Enchant enchant = enchantManager.getEnchant(enchantId);
            if (enchant == null) continue;
            if (enchant.getType() != type) continue;

            EnchantLevel enchantLevel = enchant.getLevel(level);
            if (enchantLevel == null) continue;

            String cooldownKey = player.getUniqueId() + ":" + enchantId;
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(cooldownKey);

            if (lastUse != null && (now - lastUse) < enchantLevel.getCooldown() * 1000L) {
                continue;
            }

            if (!checkConditions(enchantLevel, context)) continue;

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
        if (meta == null) return enchants;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return enchants;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null || enchantsData.isEmpty()) return enchants;

        String[] entries = enchantsData.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    enchants.put(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {}
            }
        }

        return enchants;
    }

    private boolean checkConditions(@NotNull EnchantLevel level, @NotNull Map<String, Object> context) {
        if (level.getConditions().isEmpty()) return true;

        for (String condition : level.getConditions()) {
            if (!evaluateCondition(condition, context)) {
                return false;
            }
        }

        return true;
    }

    private boolean evaluateCondition(@NotNull String condition, @NotNull Map<String, Object> context) {
        String[] parts = condition.split("=>");
        if (parts.length != 2) return true;

        String check = parts[0].trim();
        String result = parts[1].trim();

        if (check.contains("{sneaking}")) {
            Player player = (Player) context.get("player");
            boolean sneaking = player != null && player.isSneaking();
            boolean expected = check.contains("true");

            if (sneaking == expected) {
                return result.equalsIgnoreCase("allow");
            }
        }

        return result.equalsIgnoreCase("allow");
    }

    private void executeActions(@NotNull Player player, @NotNull EnchantLevel level, @NotNull Map<String, Object> context) {
        level.getActions().forEach(action -> actionExecutor.executeAction(player, action, context));
    }

    @NotNull
    private Map<String, Object> createContext(@NotNull Player player, @Nullable Object block, @Nullable Object entity, @Nullable Object event) {
        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        if (block != null) context.put("block", block);
        if (entity != null) context.put("entity", entity);
        if (event != null) context.put("event", event);
        return context;
    }
}