package com.mongenscave.mcenchants.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ItemData(
        ItemStack item,
        List<Integer> slots,
        int priority,
        @Nullable List<String> commands,
        @Nullable SoundData sound
) {}