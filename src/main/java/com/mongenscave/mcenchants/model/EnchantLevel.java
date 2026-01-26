package com.mongenscave.mcenchants.model;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Builder
public class EnchantLevel {
    private final int level;
    private final int chance;
    @NotNull private final List<String> conditions;
    private final long cooldown;
    @NotNull private final List<EnchantAction> actions;
}