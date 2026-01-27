package com.mongenscave.mcenchants.executor.condition;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ConditionEvaluator {

    public static boolean evaluateCondition(@NotNull String condition, @NotNull Map<String, Object> context, @NotNull Player player) {
        String[] parts = condition.split("=>");
        if (parts.length != 2) {
            return true;
        }

        String checkPart = parts[0].trim();
        String result = parts[1].trim();

        String conditionKey = null;
        String operator = "=";
        String value = "";

        if (checkPart.contains("{") && checkPart.contains("}")) {
            int start = checkPart.indexOf("{") + 1;
            int end = checkPart.indexOf("}");
            conditionKey = checkPart.substring(start, end).trim();

            String remainder = checkPart.substring(end + 1).trim();

            if (remainder.contains("!=")) {
                operator = "!=";
                value = remainder.substring(remainder.indexOf("!=") + 2).trim();
            } else if (remainder.contains(">=")) {
                operator = ">=";
                value = remainder.substring(remainder.indexOf(">=") + 2).trim();
            } else if (remainder.contains("<=")) {
                operator = "<=";
                value = remainder.substring(remainder.indexOf("<=") + 2).trim();
            } else if (remainder.contains(">")) {
                operator = ">";
                value = remainder.substring(remainder.indexOf(">") + 1).trim();
            } else if (remainder.contains("<")) {
                operator = "<";
                value = remainder.substring(remainder.indexOf("<") + 1).trim();
            } else if (remainder.contains("=")) {
                operator = "=";
                value = remainder.substring(remainder.indexOf("=") + 1).trim();
            }
        }

        EnchantCondition enchantCondition = null;

        if (conditionKey != null) {
            enchantCondition = ConditionRegistry.getCondition(conditionKey);
        }

        boolean conditionMet;
        if (enchantCondition != null) {
            conditionMet = enchantCondition.evaluate(player, value, context);

            if (operator.equals("!=")) {
                conditionMet = !conditionMet;
            }
        } else {
            return result.equalsIgnoreCase("allow");
        }

        if (conditionMet) {
            return result.equalsIgnoreCase("allow");
        } else {
            return result.equalsIgnoreCase("stop");
        }
    }
}