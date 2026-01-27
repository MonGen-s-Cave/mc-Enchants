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
    MISSING_ARGUMENT("messages.missing-argument"),

    WRONG_BOOK("messages.wrong-book"),
    WRONG_ENCHANT("messages.wrong-enchant"),
    NOT_APPLIABLE("messages.not-appliable"),
    ALREADY_HAS("messages.already-has"),
    SUCCESS_APPLY("messages.success-apply"),
    APPLY_FAIL("messages.apply-fail"),
    BOOK_REVEALED("messages.book-revealed"),
    RESOLVER_EMPTY_BOOK("messages.resolver-empty-book"),
    ERROR_DUE_RESOLVER("messages.error-due-resolver"),
    SUCCESS_RESOLVE("messages.success-resolve"),
    SUCCESS_DENY("messages.success-deny"),
    NOT_ENOUGH_XP("messages.not-enough-xp"),
    SUCCESS_PURCHASE("messages.success-purchase"),
    GIVEBOOK_SENDER("messages.givebook-sender"),
    GIVEENCHANT_SENDER("messages.giveenchant-sender");

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