package com.mongenscave.mcenchants.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class EnchantAction {
    @NotNull private final String type;
    private final int radius;
    private final double multiplier;
    private final int duration;

    public EnchantAction(@NotNull String actionString) {
        String[] parts = actionString.split(":");
        this.type = parts[0];

        if (parts.length > 1) {
            String[] params = parts[1].split(";");
            this.radius = parseIntParam(params, 0, 0);
            this.multiplier = parseDoubleParam(params, 1, 1.0);
            this.duration = parseIntParam(params, 2, 0);
        } else {
            this.radius = 0;
            this.multiplier = 1.0;
            this.duration = 0;
        }
    }

    private int parseIntParam(@NotNull String[] params, int index, int defaultValue) {
        try {
            if (index < params.length) {
                return Integer.parseInt(params[index].trim());
            }
        } catch (NumberFormatException ignored) {}
        return defaultValue;
    }

    private double parseDoubleParam(@NotNull String[] params, int index, double defaultValue) {
        try {
            if (index < params.length) {
                return Double.parseDouble(params[index].trim());
            }
        } catch (NumberFormatException ignored) {}
        return defaultValue;
    }
}