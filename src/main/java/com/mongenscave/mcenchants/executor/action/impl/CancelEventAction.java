package com.mongenscave.mcenchants.executor.action.impl;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CancelEventAction extends EnchantAction {
    @Override
    public void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        if (event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
    }

    @Override
    public String getActionType() {
        return "CANCEL_EVENT";
    }

    @Override
    public boolean canExecute(@NotNull Map<String, Object> context) {
        Event event = (Event) context.get("event");
        return event instanceof Cancellable;
    }
}