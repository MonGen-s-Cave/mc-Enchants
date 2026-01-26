package com.mongenscave.mcenchants.manager;

import lombok.Getter;

@Getter
public final class ManagerRegistry {
    private final CategoryManager categoryManager;
    private final EnchantManager enchantManager;
    private final BookManager bookManager;

    public ManagerRegistry() {
        this.categoryManager = new CategoryManager();
        this.enchantManager = new EnchantManager(categoryManager);
        this.bookManager = new BookManager(enchantManager, categoryManager);
    }

    public void reload() {
        categoryManager.reload();
        enchantManager.reload();
    }
}