package com.mongenscave.mcenchants.manager;

import com.mongenscave.mcenchants.model.EnchantRemoverTable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantRemoverManager {
    private final Map<Location, EnchantRemoverTable> tables = new ConcurrentHashMap<>();

    public void registerTable(@NotNull Location location) {
        tables.put(location, new EnchantRemoverTable(location));
    }

    public void unregisterTable(@NotNull Location location) {
        EnchantRemoverTable table = tables.remove(location);
        if (table != null && table.getItemDisplay() != null) {
            table.getItemDisplay().remove();
        }
    }

    @Nullable
    public EnchantRemoverTable getTable(@NotNull Location location) {
        return tables.get(location);
    }

    public boolean isTable(@NotNull Location location) {
        return tables.containsKey(location);
    }

    public void clearAll() {
        tables.values().forEach(table -> {
            if (table.getItemDisplay() != null) {
                table.getItemDisplay().remove();
            }
        });
        tables.clear();
    }
}