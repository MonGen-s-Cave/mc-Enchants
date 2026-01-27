package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DoubleDamageAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) {
            return;
        }

        double currentDamage = damageEvent.getDamage();
        double newDamage = currentDamage * 2.0;
        damageEvent.setDamage(newDamage);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.2f);
    }

    @Override
    public String getActionType() {
        return "DOUBLE_DAMAGE";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        return event instanceof EntityDamageByEntityEvent;
    }
}