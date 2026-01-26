package com.mongenscave.mcenchants.model;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@Builder
public class EnchantAction {
    @NotNull private final String actionType;
    private final int radius;
    private final double multiplier;
    private final int duration;

    public static EnchantAction fromString(@NotNull String actionString) {
        String[] parts = actionString.split(":");
        String actionType = parts[0].trim();

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

        return EnchantAction.builder()
                .actionType(actionType)
                .radius(radius)
                .multiplier(multiplier)
                .duration(duration)
                .build();
    }
}