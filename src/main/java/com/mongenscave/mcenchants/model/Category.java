package com.mongenscave.mcenchants.model;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Getter
@Builder
public class Category {
    private static final Random RANDOM = new Random();

    @NotNull private final String id;
    @NotNull private final String color;
    @NotNull private final String name;
    private final int minDustSuccess;
    private final int maxDustSuccess;
    private final int price;

    public int getRandomDustSuccess() {
        if (minDustSuccess == maxDustSuccess) return minDustSuccess;
        return RANDOM.nextInt(maxDustSuccess - minDustSuccess + 1) + minDustSuccess;
    }

    public static Category fromConfig(@NotNull String id, @NotNull String color, @NotNull String name, @NotNull String dustSuccess, int price) {
        String[] parts = dustSuccess.split("-");
        int min = 0;
        int max = 0;

        if (parts.length == 2) {
            try {
                min = Integer.parseInt(parts[0]);
                max = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        } else if (parts.length == 1) {
            try {
                min = max = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ignored) {}
        }

        return Category.builder()
                .id(id)
                .color(color)
                .name(name)
                .minDustSuccess(min)
                .maxDustSuccess(max)
                .price(price)
                .build();
    }
}