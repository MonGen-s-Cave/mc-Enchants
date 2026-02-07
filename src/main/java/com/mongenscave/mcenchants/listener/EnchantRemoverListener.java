package com.mongenscave.mcenchants.listener;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.item.ItemFactory;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.manager.EnchantRemoverManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantRemoverTable;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import com.mongenscave.mcenchants.util.SoundUtil;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantRemoverListener implements Listener {
    private final EnchantRemoverManager removerManager;
    private final EnchantManager enchantManager;
    private final BookManager bookManager;
    private MyScheduledTask timeoutCheckTask;

    public EnchantRemoverListener() {
        this.removerManager = McEnchants.getInstance().getManagerRegistry().getEnchantRemoverManager();
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        startTimeoutChecker();
    }

    private void startTimeoutChecker() {
        timeoutCheckTask = McEnchants.getInstance().getScheduler().runTaskTimer(() -> {
            long timeoutSeconds = ConfigKey.REMOVER_TABLE_TIMEOUT.getInt();
            long timeoutMillis = timeoutSeconds * 1000L;

            removerManager.getAllTables().forEach((location, table) -> {
                if (table.isExpired(timeoutMillis)) {
                    returnItemToWorld(table);
                }
            });
        }, 20L, 20L);
    }

    public void shutdown() {
        if (timeoutCheckTask != null) timeoutCheckTask.cancel();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTablePlace(@NotNull BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (!ItemFactory.isEnchantRemoverTable(item)) return;

        Location blockLoc = event.getBlock().getLocation();
        removerManager.registerTable(blockLoc);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTableBreak(@NotNull BlockBreakEvent event) {
        Location blockLoc = event.getBlock().getLocation();

        if (!removerManager.isTable(blockLoc)) return;

        EnchantRemoverTable table = removerManager.getTable(blockLoc);

        if (table == null) return;

        if (table.getPlacedItem() != null) blockLoc.getWorld().dropItemNaturally(blockLoc, table.getPlacedItem());
        if (table.getItemDisplay() != null) table.getItemDisplay().remove();

        event.setDropItems(false);
        blockLoc.getWorld().dropItemNaturally(blockLoc, ItemFactory.createEnchantRemoverTable());

        removerManager.unregisterTable(blockLoc);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTableInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        Location blockLoc = event.getClickedBlock().getLocation();

        if (!removerManager.isTable(blockLoc)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        EnchantRemoverTable table = removerManager.getTable(blockLoc);

        if (table == null) return;

        if (handItem.getType().isAir() && table.getPlacedItem() != null) {
            retrieveItem(player, table, blockLoc);
            return;
        }

        if (handItem.getType() == Material.BRUSH) {
            handleBrushSwipe(player, table, blockLoc);
            return;
        }

        if (table.getPlacedItem() != null) {
            player.sendMessage(MessageKey.REMOVER_ITEM_PLACED.getMessage());
            return;
        }

        Map<String, Integer> enchants = getEnchantsFromItem(handItem);

        if (enchants.isEmpty()) {
            player.sendMessage(MessageKey.REMOVER_NO_ENCHANTS.getMessage());
            return;
        }

        placeItem(player, table, handItem, enchants, blockLoc);
    }

    private void placeItem(@NotNull Player player, @NotNull EnchantRemoverTable table,
                           @NotNull ItemStack item, @NotNull Map<String, Integer> enchants,
                           @NotNull Location blockLoc) {
        ItemStack itemCopy = item.clone();
        itemCopy.setAmount(1);

        table.setPlacedItem(itemCopy);

        enchants.forEach(table::addEnchant);

        createItemDisplay(table, itemCopy, blockLoc);

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);

        player.sendMessage(MessageKey.REMOVER_ITEM_PLACED.getMessage());
        SoundUtil.playSuccessSound(player);
    }

    private void createItemDisplay(@NotNull EnchantRemoverTable table,
                                   @NotNull ItemStack item,
                                   @NotNull Location blockLoc) {
        float scale = ConfigKey.REMOVER_TABLE_SCALE.getFloat();
        double yOffset = ConfigKey.REMOVER_TABLE_Y_OFFSET.getDouble();

        Location displayLoc = blockLoc.clone().add(0.5, yOffset, 0.5);

        ItemDisplay display = blockLoc.getWorld().spawn(displayLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setDisplayWidth(scale);
            entity.setDisplayHeight(scale);
            entity.setBillboard(Display.Billboard.FIXED);

            Transformation transformation = new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f((float)Math.toRadians(90), 1, 0, 0),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 1, 0)
            );
            entity.setTransformation(transformation);

            entity.setInterpolationDuration(0);
            entity.setInterpolationDelay(0);
        });

        table.setItemDisplay(display);
    }

    private void handleBrushSwipe(@NotNull Player player, @NotNull EnchantRemoverTable table, @NotNull Location blockLoc) {
        if (table.getPlacedItem() == null) return;

        if (!table.hasEnchants()) {
            returnItemToWorld(table);
            player.sendMessage(MessageKey.REMOVER_ALL_REMOVED.getMessage());
            return;
        }

        String currentEnchant = table.getNextEnchant();
        if (currentEnchant == null) return;

        table.incrementSwipe(currentEnchant);

        int requiredSwipes = ConfigKey.REMOVER_BRUSH_SWIPES.getInt();

        playBrushAnimation(blockLoc, player);

        if (table.getSwipeProgress(currentEnchant) >= requiredSwipes) {
            removeEnchantFromTable(player, table, currentEnchant, blockLoc);
        }
    }

    private void removeEnchantFromTable(@NotNull Player player, @NotNull EnchantRemoverTable table,
                                        @NotNull String enchantId, @NotNull Location blockLoc) {
        int level = table.getRemainingEnchants().get(enchantId);
        Enchant enchant = enchantManager.getEnchant(enchantId);

        if (enchant == null) return;

        int minSuccess = ConfigKey.REMOVER_SUCCESS_MIN.getInt();
        int maxSuccess = ConfigKey.REMOVER_SUCCESS_MAX.getInt();

        int newSuccessRate = ThreadLocalRandom.current().nextInt(minSuccess, maxSuccess + 1);
        int newDestroyRate = 100 - newSuccessRate;

        ItemStack book = bookManager.createRevealedBook(enchantId, level, newSuccessRate, newDestroyRate);
        blockLoc.getWorld().dropItemNaturally(blockLoc.clone().add(0.5, 1.5, 0.5), book);

        table.removeEnchant(enchantId);

        removeEnchantFromItem(table.getPlacedItem(), enchantId);
        removeEnchantLoreFromItem(table.getPlacedItem(), enchant, level);

        if (table.getItemDisplay() != null) table.getItemDisplay().setItemStack(table.getPlacedItem());

        SoundUtil.playSuccessSound(player);
        blockLoc.getWorld().spawnParticle(Particle.ENCHANT, blockLoc.clone().add(0.5, 1.5, 0.5), 20);

        if (!table.hasEnchants()) {
            returnItemToWorld(table);
            player.sendMessage(MessageKey.REMOVER_ALL_REMOVED.getMessage());
        }
    }

    private void playBrushAnimation(@NotNull Location location, @NotNull Player player) {
        String sound = ConfigKey.REMOVER_BRUSH_SOUND.getString();
        player.playSound(location, Sound.valueOf(sound), 1.0f, 1.0f);

        String particleType = ConfigKey.REMOVER_BRUSH_PARTICLE.getString();

        location.getWorld().spawnParticle(
                Particle.valueOf(particleType),
                location.clone().add(0.5, 1.3, 0.5),
                5,
                0.3, 0.1, 0.3,
                0.01
        );
    }

    private void retrieveItem(@NotNull Player player, @NotNull EnchantRemoverTable table,
                              @NotNull Location blockLoc) {
        returnItemToWorld(table);
        player.sendMessage(MessageKey.REMOVER_ITEM_RETRIEVED.getMessage());
        SoundUtil.playSuccessSound(player);
    }

    private void returnItemToWorld(@NotNull EnchantRemoverTable table) {
        if (table.getPlacedItem() == null) return;

        Location blockLoc = table.getLocation();
        blockLoc.getWorld().dropItemNaturally(blockLoc.clone().add(0.5, 1.2, 0.5), table.getPlacedItem());

        if (table.getItemDisplay() != null) {
            table.getItemDisplay().remove();
            table.setItemDisplay(null);
        }

        table.setPlacedItem(null);
        table.getRemainingEnchants().clear();
        table.getSwipeProgress().clear();
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
                    enchants.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        return enchants;
    }

    private void removeEnchantFromItem(@NotNull ItemStack item, @NotNull String enchantId) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(McEnchants.getInstance(), "enchants");

        if (!pdc.has(key, PersistentDataType.STRING)) return;

        String enchantsData = pdc.get(key, PersistentDataType.STRING);
        if (enchantsData == null) return;

        String[] entries = enchantsData.split(";");
        StringBuilder newData = new StringBuilder();

        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length == 2 && !parts[0].trim().equals(enchantId)) {
                if (!newData.isEmpty()) newData.append(";");
                newData.append(entry);
            }
        }

        if (!newData.isEmpty()) {
            pdc.set(key, PersistentDataType.STRING, newData.toString());
        } else {
            pdc.remove(key);
        }

        item.setItemMeta(meta);
    }

    private void removeEnchantLoreFromItem(@NotNull ItemStack item, @NotNull Enchant enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!meta.hasLore()) return;

        List<String> lore = new ArrayList<>(meta.getLore());

        if (!enchant.isItemLoreEnabled() || enchant.getItemLoreLines().isEmpty()) {
            return;
        }

        String romanLevel = getRomanNumeral(level);

        List<String> linesToRemove = new ArrayList<>();
        for (String line : enchant.getItemLoreLines()) {
            String processedLine = MessageProcessor.process(line
                    .replace("{categoryColor}", enchant.getCategory().getColor())
                    .replace("{level}", romanLevel));
            linesToRemove.add(processedLine);
        }

        lore.removeAll(linesToRemove);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @NotNull
    private String getRomanNumeral(int number) {
        List<String> levels = McEnchants.getInstance().getLevels().getList("levels");

        for (String level : levels) {
            String[] parts = level.split(":");
            if (parts.length == 2) {
                try {
                    if (Integer.parseInt(parts[0]) == number) {
                        return parts[1];
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return String.valueOf(number);
    }
}