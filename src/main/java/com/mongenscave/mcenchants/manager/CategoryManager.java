package com.mongenscave.mcenchants.manager;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.model.Category;
import com.mongenscave.mcenchants.util.LoggerUtil;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CategoryManager {
    private final Map<String, Category> categories = new ConcurrentHashMap<>();

    public CategoryManager() {
        loadCategories();
    }

    public void loadCategories() {
        categories.clear();

        Config config = McEnchants.getInstance().getCategory();
        Section categoriesSection = config.getSection("categories");

        if (categoriesSection == null) {
            LoggerUtil.warn("0 category");
            return;
        }

        for (String categoryId : categoriesSection.getRoutesAsStrings(false)) {
            Section categorySection = categoriesSection.getSection(categoryId);
            if (categorySection == null) continue;

            try {
                String color = categorySection.getString("color", "&#FFFFFF");
                String name = categorySection.getString("name", categoryId);
                String dustSuccess = categorySection.getString("dust-success", "0");
                int price = categorySection.getInt("price", 0);

                Category category = Category.fromConfig(categoryId, color, name, dustSuccess, price);
                categories.put(categoryId, category);

                LoggerUtil.info("Loaded category: " + categoryId);
            } catch (Exception exception) {
                LoggerUtil.error(exception.getMessage());
            }
        }
    }

    @NotNull
    public Category getCategory(@NotNull String id) {
        return categories.getOrDefault(id, Category.builder()
                .id(id)
                .color("&#FFFFFF")
                .name(id)
                .minDustSuccess(0)
                .maxDustSuccess(0)
                .price(0)
                .build());
    }

    public void reload() {
        loadCategories();
    }
}