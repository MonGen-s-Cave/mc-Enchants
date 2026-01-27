package com.mongenscave.mcenchants.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public record ActionData(
        @NotNull String fullActionString,
        @NotNull String actionType,
        int radius,
        double multiplier,
        int duration
) {

    @Contract("_ -> new")
    public static @NonNull ActionData fromString(@NotNull String actionString) {
        String[] initialSplit = actionString.split(":", 2);
        String baseAction = initialSplit[0].trim().toUpperCase();

        if (baseAction.equals("POTION")) {
            return new ActionData(actionString, baseAction, 0, 1.0, 0);
        }

        String[] parts = actionString.split(":");
        String actionType = parts[0].trim().toUpperCase();

        int radius = 0;
        double multiplier = 1.0;
        int duration = 0;

        if (parts.length > 1) {
            String[] params = parts[1].split(";");
            if (params.length > 0) {
                try {
                    radius = Integer.parseInt(params[0].trim());
                } catch (NumberFormatException ignored) {}
            }
            if (params.length > 1) {
                try {
                    multiplier = Double.parseDouble(params[1].trim());
                } catch (NumberFormatException ignored) {}
            }
            if (params.length > 2) {
                try {
                    duration = Integer.parseInt(params[2].trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        return new ActionData(actionString, actionType, radius, multiplier, duration);
    }
}