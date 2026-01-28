package com.mongenscave.mcenchants.listener;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.identifier.key.MessageKey;
import com.mongenscave.mcenchants.manager.BookManager;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import com.mongenscave.mcenchants.util.SoundUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class BookRevealListener implements Listener {
    private final BookManager bookManager;

    public BookRevealListener() {
        this.bookManager = McEnchants.getInstance().getManagerRegistry().getBookManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!bookManager.isMysteriousBook(item)) return;

        event.setCancelled(true);

        ItemStack revealedBook = bookManager.revealBook(item);

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);

        player.getInventory().addItem(revealedBook);
        player.sendMessage(MessageKey.BOOK_REVEALED.getMessage());
        SoundUtil.playSuccessSound(player);
    }
}