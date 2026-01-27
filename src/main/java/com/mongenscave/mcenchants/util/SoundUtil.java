package com.mongenscave.mcenchants.util;

import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class SoundUtil {

    public void playSound(@NotNull Player player, @NotNull ConfigKey configKey) {
        String soundName = configKey.getString();
        playSound(player, soundName, 1.0f, 1.0f);
    }

    public void playSound(@NotNull Player player, @NotNull String soundName, float volume, float pitch) {
        if (soundName.isEmpty()) return;

        Location location = player.getLocation();

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase().replace(".", "_"));
            player.playSound(location, sound, volume, pitch);
        } catch (IllegalArgumentException exception) {
            player.playSound(location, soundName, volume, pitch);
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