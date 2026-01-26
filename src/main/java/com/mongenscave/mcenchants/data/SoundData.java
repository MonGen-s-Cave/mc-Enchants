package com.mongenscave.mcenchants.data;

import org.jetbrains.annotations.NotNull;

public record SoundData(
        @NotNull String name,
        float volume,
        float pitch
) {}
