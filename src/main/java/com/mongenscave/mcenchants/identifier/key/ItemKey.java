package com.mongenscave.mcenchants.identifier.key;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.item.ItemFactory;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public enum ItemKey {
    TESZT("placeholder");

    private final String path;

    ItemKey(@NotNull final String path) {
        this.path = path;
    }

    public ItemStack getItem() {
        return ItemFactory.createItemFromString(path, McEnchants.getInstance().getGuis()).orElse(null);
    }

    public List<String> getList() {
        return McEnchants.getInstance().getGuis().getList(path);
    }

    public int getSlot() {
        return McEnchants.getInstance().getGuis().getInt(path + ".slot");
    }

    public boolean matchesSlot(int slot) {
        Object slotConfig = McEnchants.getInstance().getGuis().get(path + ".slot");

        if (slotConfig instanceof Integer) {
            return slot == (Integer) slotConfig;
        }

        if (slotConfig instanceof String slotStr) {
            String[] parts = slotStr.split(",");
            for (String part : parts) {
                part = part.trim();

                if (part.contains("-")) {
                    String[] range = part.split("-");
                    if (range.length == 2) {
                        try {
                            int start = Integer.parseInt(range[0].trim());
                            int end = Integer.parseInt(range[1].trim());
                            if (slot >= Math.min(start, end) && slot <= Math.max(start, end)) {
                                return true;
                            }

                        } catch (NumberFormatException ignored) {}
                    }
                } else {
                    try {
                        int configSlot = Integer.parseInt(part);
                        if (slot == configSlot) {
                            return true;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return false;
    }
}