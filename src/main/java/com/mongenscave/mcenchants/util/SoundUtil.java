package com.mongenscave.mcenchants.util;

import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class SoundUtil {
    public void playSound(@NotNull Player player, @NotNull ConfigKey configKey) {
        String soundName = configKey.getString();
        playSound(player, soundName);
    }

    private void playSound(@NotNull Player player, @NotNull String soundName) {
        if (soundName.isEmpty()) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, (float) 1.0, (float) 1.0);
        } catch (IllegalArgumentException exception) {
            player.playSound(player.getLocation(), soundName, (float) 1.0, (float) 1.0);
        }
    }

    public void playErrorSound(@NotNull Player player) {
        playSound(player, ConfigKey.SOUND_ERROR);
    }

    public void playSuccessSound(@NotNull Player player) {
        playSound(player, ConfigKey.SOUND_SUCCESS);
    }

    public void playOpenGuiSound(@NotNull Player player) {
        playSound(player, ConfigKey.SOUND_OPEN_GUI);
    }
}