package com.mongenscave.mcenchants.executor.action;

import com.mongenscave.mcenchants.data.ActionData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class EnchantAction {
    public abstract void execute(@NotNull Player player, @NotNull ActionData actionData, @NotNull Map<String, Object> context);

    public abstract String getActionType();

    public boolean canExecute(@NotNull Map<String, Object> context) {
        return true;
    }
}