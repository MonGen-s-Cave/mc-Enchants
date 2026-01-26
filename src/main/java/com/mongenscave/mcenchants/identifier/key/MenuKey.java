package com.mongenscave.mcenchants.identifier.key;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.processor.MessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum MenuKey {
    MENU_MAIN_TITLE("main-menu.title"),
    MENU_MAIN_SIZE("main-menu.size"),

    MENU_REPAIRER_TITLE("repairer-menu.title"),
    MENU_REPAIRER_SIZE("repairer-menu.size"),
    MENU_REPAIRER_BOOK_INPUT("repairer-menu.book-input-slot"),
    MENU_REPAIRER_DUST_INPUT("repairer-menu.dust-input-slot"),
    MENU_REPAIRER_OUTPUT("repairer-menu.output-slot"),

    MENU_RESOLVER_TITLE("resolver-menu.title"),
    MENU_RESOLVER_SIZE("resolver-menu.size"),
    MENU_RESOLVER_PLACEABLE_SLOTS("resolver-menu.placeable-slots"),

    MENU_ENCHANTER_TITLE("enchanter-menu.title"),
    MENU_ENCHANTER_SIZE("enchanter-menu.size");


    private static final Config config = McEnchants.getInstance().getGuis();
    private final String path;

    MenuKey(@NotNull String path) {
        this.path = path;
    }

    public static @NotNull String getString(@NotNull String path) {
        return config.getString(path);
    }

    public @NotNull String getString() {
        return MessageProcessor.process(config.getString(path));
    }

    public @NotNull String getPath() {
        return path;
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