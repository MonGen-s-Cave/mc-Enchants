package com.mongenscave.mcenchants.identifier.key;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public enum ConfigKey {
    ALIASES("aliases"),

    SOUND_ERROR("sounds.error"),
    SOUND_SUCCESS("sounds.success"),
    SOUND_OPEN_GUI("sounds.open-gui"),

    ENCHANT_PER_ITEM("enchant-per-item"),

    IGNORED_MATERIALS("ignored-materials"),
    BLACKLISTED_WORLDS("blacklisted-worlds");

    private final String path;
    private static final Config config = McEnchants.getInstance().getConfiguration();

    ConfigKey(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getString() {
        return MessageProcessor.process(config.getString(path));
    }

    public static @NotNull String getString(@NotNull String path) {
        return config.getString(path);
    }

    public boolean getBoolean() {
        return config.getBoolean(path);
    }

    public int getInt() {
        return config.getInt(path);
    }

    public List<String> getList() {
        return config.getList(path);
    }
}