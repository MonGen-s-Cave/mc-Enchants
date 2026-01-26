package com.mongenscave.mcenchants.identifier.key;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Getter
public enum MessageKey {

    // COMMON

    RELOAD("messages.reload"),
    NO_PERMISSION("messages.no-permission"),
    PLAYER_REQUIRED("messages.player-required"),
    PLAYER_NOT_FOUND("messages.player-not-found"),
    MISSING_ARGUMENT("messages.missing-argument");

    private final String path;
    private static final Config config = McEnchants.getInstance().getLanguage();

    MessageKey(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getMessage() {
        return MessageProcessor.process(config.getString(path))
                .replace("%prefix%", MessageProcessor.process(config.getString("prefix")));
    }

    public @NonNull @Unmodifiable List<String> getMessages() {
        return config.getStringList(path)
                .stream()
                .toList();
    }
}