package com.mongenscave.mcenchants.listener;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.manager.EnchantManager;
import com.mongenscave.mcenchants.model.Enchant;
import com.mongenscave.mcenchants.model.EnchantRemoverTable;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import com.mongenscave.mcenchants.util.SoundUtil;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("deprecation")
public class EnchantRemoverListener implements Listener {
    private final EnchantManager enchantManager;
    private final BookManager bookManager;
    private MyScheduledTask timeoutCheckTask;
    private MyScheduledTask brushProgressTask;
    private final Map<UUID, EnchantRemoverTable> playerTables = new ConcurrentHashMap<>();
    private final Map<UUID, BrushProgress> activeBrushing = new ConcurrentHashMap<>();

    private static class BrushProgress {
        int ticksSinceLastRemoval;

        BrushProgress() {
            this.ticksSinceLastRemoval = 0;
        }
    }

    public EnchantRemoverListener() {
        this.enchantManager = McEnchants.getInstance().getManagerRegistry().getEnchantManager();
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
        startTimeoutChecker();
        startBrushProgressTracker();
    }

    private void startTimeoutChecker() {
        timeoutCheckTask = McEnchants.getInstance().getScheduler().runTaskTimer(() -> {
            long timeoutSeconds = ConfigKey.REMOVER_TIMEOUT.getInt();
            long timeoutMillis = timeoutSeconds * 1000L;

            playerTables.entrySet().removeIf(entry -> {
                EnchantRemoverTable table = entry.getValue();
                if (table.isExpired(timeoutMillis)) {
                    returnItemToWorld(table);
                    return true;
                }
                return false;
            });
        }, 20L, 20L);
    }

    private void startBrushProgressTracker() {
        brushProgressTask = McEnchants.getInstance().getScheduler().runTaskTimer(() -> {
            Iterator<Map.Entry<UUID, BrushProgress>> iterator = activeBrushing.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, BrushProgress> entry = iterator.next();
                UUID playerId = entry.getKey();
                BrushProgress progress = entry.getValue();
                Player player = McEnchants.getInstance().getServer().getPlayer(playerId);

                if (player == null || !player.isOnline()) {
                    iterator.remove();
                    continue;
                }

                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() != Material.BRUSH) {
                    iterator.remove();
                    continue;
                }

                EnchantRemoverTable table = playerTables.get(playerId);
                if (table == null || table.getPlacedItem() == null) {
                    iterator.remove();
                    continue;
                }

                if (player.getLocation().distance(table.getLocation()) > 5.0) {
                    iterator.remove();
                    continue;
                }

                if (!player.isSneaking() && hand.getType() == Material.BRUSH) {
                    progress.ticksSinceLastRemoval++;

                    if (progress.ticksSinceLastRemoval % 3 == 0) {
                        playBrushAnimation(table.getLocation(), player);
                    }

                    if (progress.ticksSinceLastRemoval >= 12) {
                        progress.ticksSinceLastRemoval = 0;
                        removeOneEnchant(player);
                    }
                }
            }
        }, 1L, 1L);
    }

    public void shutdown() {
        if (timeoutCheckTask != null) timeoutCheckTask.cancel();
        if (brushProgressTask != null) brushProgressTask.cancel();

        playerTables.values().forEach(this::returnItemToWorld);
        playerTables.clear();
        activeBrushing.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!ConfigKey.REMOVER_ENABLED.getBoolean()) return;

        String configuredBlock = ConfigKey.REMOVER_INTERACTION_BLOCK.getString();
        if (configuredBlock.isEmpty()) return;

        if (!isMatchingBlock(block, configuredBlock)) return;

        Location blockLoc = block.getLocation();
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem.getType() == Material.BRUSH) {
            EnchantRemoverTable table = playerTables.get(player.getUniqueId());

            if (table == null || table.getPlacedItem() == null) {
                event.setCancelled(true);
                return;
            }

            if (!table.getLocation().equals(blockLoc)) {
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(false);

                activeBrushing.computeIfAbsent(player.getUniqueId(), k -> new BrushProgress());
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        if (handItem.getType().isAir()) {
            EnchantRemoverTable table = playerTables.get(player.getUniqueId());
            if (table != null && table.getPlacedItem() != null) {
                retrieveItem(player, table);
            }
            return;
        }

        EnchantRemoverTable existingTable = playerTables.get(player.getUniqueId());
        if (existingTable != null && existingTable.getPlacedItem() != null) {
            player.sendMessage(MessageKey.REMOVER_ITEM_PLACED.getMessage());
            return;
        }

        Map<String, Integer> enchants = getEnchantsFromItem(handItem);

        if (enchants.isEmpty()) {
            player.sendMessage(MessageKey.REMOVER_NO_ENCHANTS.getMessage());
            return;
        }

        placeItem(player, handItem, enchants, blockLoc);
    }

    private boolean isMatchingBlock(@NotNull Block block, @NotNull String blockString) {
        if (blockString.contains(":")) {
            String[] parts = blockString.split(":", 2);
            if (parts.length != 2 || !parts[0].equalsIgnoreCase("nexo")) return false;

            return isNexoBlock(block, parts[1]);
        }

        return isVanillaBlock(block, blockString);
    }

    private boolean isNexoBlock(@NotNull Block block, @NotNull String expectedId) {
        if (!McEnchants.getInstance().getServer().getPluginManager().isPluginEnabled("Nexo")) {
            return false;
        }

        try {
            CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(block.getLocation());
            if (mechanic == null) return false;

            String blockId = mechanic.getItemID();
            return blockId.equals(expectedId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isVanillaBlock(@NotNull Block block, @NotNull String materialName) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return block.getType() == material;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @EventHandler
    public void onItemHeldChange(@NotNull PlayerItemHeldEvent event) {
        activeBrushing.remove(event.getPlayer().getUniqueId());
    }

    private void placeItem(@NotNull Player player, @NotNull ItemStack item,
                           @NotNull Map<String, Integer> enchants, @NotNull Location blockLoc) {
        ItemStack itemCopy = item.clone();
        itemCopy.setAmount(1);

        EnchantRemoverTable table = new EnchantRemoverTable(blockLoc);
        table.setPlacedItem(itemCopy);
        enchants.forEach(table::addEnchant);

        createItemDisplay(table, itemCopy, blockLoc);

        playerTables.put(player.getUniqueId(), table);

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);

        player.sendMessage(MessageKey.REMOVER_ITEM_PLACED.getMessage());
        SoundUtil.playSuccessSound(player);
    }

    private void createItemDisplay(@NotNull EnchantRemoverTable table,
                                   @NotNull ItemStack item,
                                   @NotNull Location blockLoc) {
        float scale = ConfigKey.REMOVER_SCALE.getFloat();
        double yOffset = ConfigKey.REMOVER_Y_OFFSET.getDouble();

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

    private void removeOneEnchant(@NotNull Player player) {
        EnchantRemoverTable table = playerTables.get(player.getUniqueId());
        if (table == null) {
            activeBrushing.remove(player.getUniqueId());
            return;
        }

        if (table.getPlacedItem() == null) {
            activeBrushing.remove(player.getUniqueId());
            return;
        }

        if (!table.hasEnchants()) {
            returnItemToWorld(table);
            playerTables.remove(player.getUniqueId());
            player.sendMessage(MessageKey.REMOVER_ALL_REMOVED.getMessage());
            activeBrushing.remove(player.getUniqueId());
            return;
        }

        String currentEnchant = table.getNextEnchant();
        if (currentEnchant == null) {
            activeBrushing.remove(player.getUniqueId());
            return;
        }

        int level = table.getRemainingEnchants().get(currentEnchant);
        Enchant enchant = enchantManager.getEnchant(currentEnchant);

        if (enchant == null) return;

        playBrushAnimation(table.getLocation(), player);

        table.removeEnchant(currentEnchant);
        removeEnchantFromItem(table.getPlacedItem(), currentEnchant);
        removeEnchantLoreFromItem(table.getPlacedItem(), enchant, level);

        if (table.getItemDisplay() != null) {
            table.getItemDisplay().setItemStack(table.getPlacedItem());
        }

        int minSuccess = ConfigKey.REMOVER_SUCCESS_MIN.getInt();
        int maxSuccess = ConfigKey.REMOVER_SUCCESS_MAX.getInt();
        int newSuccessRate = ThreadLocalRandom.current().nextInt(minSuccess, maxSuccess + 1);
        int newDestroyRate = 100 - newSuccessRate;

        ItemStack book = bookManager.createRevealedBook(currentEnchant, level, newSuccessRate, newDestroyRate);
        table.getLocation().getWorld().dropItemNaturally(
                table.getLocation().clone().add(0.5, 1.5, 0.5),
                book
        );

        SoundUtil.playSuccessSound(player);
        table.getLocation().getWorld().spawnParticle(
                Particle.ENCHANT,
                table.getLocation().clone().add(0.5, 1.5, 0.5),
                20
        );

        if (!table.hasEnchants()) {
            returnItemToWorld(table);
            playerTables.remove(player.getUniqueId());
            player.sendMessage(MessageKey.REMOVER_ALL_REMOVED.getMessage());
            activeBrushing.remove(player.getUniqueId());
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

    private void retrieveItem(@NotNull Player player, @NotNull EnchantRemoverTable table) {
        returnItemToWorld(table);
        playerTables.remove(player.getUniqueId());
        player.sendMessage(MessageKey.REMOVER_ITEM_RETRIEVED.getMessage());
        SoundUtil.playSuccessSound(player);
        activeBrushing.remove(player.getUniqueId());
    }

    private void returnItemToWorld(@NotNull EnchantRemoverTable table) {
        if (table.getPlacedItem() == null) return;

        Location blockLoc = table.getLocation();
        blockLoc.getWorld().dropItemNaturally(
                blockLoc.clone().add(0.5, 1.2, 0.5),
                table.getPlacedItem()
        );

        if (table.getItemDisplay() != null) {
            table.getItemDisplay().remove();
            table.setItemDisplay(null);
        }

        table.setPlacedItem(null);
        table.getRemainingEnchants().clear();
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

        if (!newData.isEmpty()) pdc.set(key, PersistentDataType.STRING, newData.toString());
        else pdc.remove(key);

        item.setItemMeta(meta);
    }

    private void removeEnchantLoreFromItem(@NotNull ItemStack item, @NotNull Enchant enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        List<String> lore = new ArrayList<>(meta.getLore());

        if (!enchant.isItemLoreEnabled() || enchant.getItemLoreLines().isEmpty()) return;

        String romanLevel = getRomanNumeral(level);

        List<String> processedPatterns = enchant.getItemLoreLines().stream()
                .map(line -> MessageProcessor.process(line
                        .replace("{categoryColor}", enchant.getCategory().getColor())
                        .replace("{level}", romanLevel)))
                .map(this::normalize)
                .toList();

        lore.removeIf(existing -> processedPatterns.stream().anyMatch(pattern -> normalize(existing).equals(pattern)));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @NotNull
    private String normalize(String line) {
        return ChatColor.stripColor(line).trim();
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

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        activeBrushing.remove(event.getPlayer().getUniqueId());

        EnchantRemoverTable table = playerTables.remove(event.getPlayer().getUniqueId());
        if (table != null) returnItemToWorld(table);
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        BrushProgress progress = activeBrushing.get(event.getPlayer().getUniqueId());
        if (progress == null) return;

        EnchantRemoverTable table = playerTables.get(event.getPlayer().getUniqueId());
        if (table == null) return;

        if (event.getPlayer().getLocation().distance(table.getLocation()) > 5.0) activeBrushing.remove(event.getPlayer().getUniqueId());
    }
}