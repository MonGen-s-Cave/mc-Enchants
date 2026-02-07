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
    BLACKLISTED_WORLDS("blacklisted-worlds"),

    REMOVER_TABLE_ENABLED("enchant-remover-table.enabled"),
    REMOVER_TABLE_MATERIAL("enchant-remover-table.block-material"),
    REMOVER_TABLE_DISPLAY_NAME("enchant-remover-table.display-name"),
    REMOVER_TABLE_LORE("enchant-remover-table.lore"),
    REMOVER_TABLE_MODEL_DATA("enchant-remover-table.custom-model-data"),
    REMOVER_TABLE_TIMEOUT("enchant-remover-table.timeout-seconds"),
    REMOVER_TABLE_SCALE("enchant-remover-table.item-display.scale"),
    REMOVER_TABLE_Y_OFFSET("enchant-remover-table.item-display.y-offset"),
    REMOVER_BRUSH_SWIPES("enchant-remover-table.brush.required-swipes"),
    REMOVER_BRUSH_PARTICLE("enchant-remover-table.brush.particle-effect"),
    REMOVER_BRUSH_SOUND("enchant-remover-table.brush.sound-effect"),
    REMOVER_SUCCESS_MIN("enchant-remover-table.new-success-rates.min"),
    REMOVER_SUCCESS_MAX("enchant-remover-table.new-success-rates.max");

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

    public double getDouble() {
        return config.getDouble(path);
    }

    public float getFloat() {
        return config.getFloat(path);
    }

    public List<String> getList() {
        return config.getList(path);
    }
}