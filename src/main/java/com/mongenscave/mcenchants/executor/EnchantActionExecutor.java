package com.mongenscave.mcenchants.executor;

import com.mongenscave.mcenchants.data.ActionData;
import com.mongenscave.mcenchants.executor.action.ActionRegistry;
import com.mongenscave.mcenchants.executor.action.EnchantAction;
import com.mongenscave.mcenchants.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EnchantActionExecutor {
    public void executeAction(@NotNull Player player, @NotNull EnchantAction actionModel, @NotNull Map<String, Object> context) {
        try {
            String actionTypeString = actionModel.getActionType();
            ActionData actionData = ActionData.fromString(actionTypeString);
            String baseType = actionData.actionType();

            LoggerUtil.info("Executing action - Full: " + actionTypeString + ", Base: " + baseType);

            EnchantAction action = ActionRegistry.getAction(baseType);

            if (action == null) {
                LoggerUtil.warn("Unknown action type: " + baseType);
                return;
            }

            if (!action.canExecute(context)) {
                LoggerUtil.warn("Action " + baseType + " cannot be executed with current context");
                return;
            }

            action.execute(player, actionData, context);

        } catch (Exception exception) {
            LoggerUtil.error("Error executing action " + actionModel.getActionType() + ": " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}