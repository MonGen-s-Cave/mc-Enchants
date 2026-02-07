package com.mongenscave.mcenchants.manager;

import com.mongenscave.mcenchants.executor.action.impl.BreakBlockAction;
import lombok.Getter;

@Getter
public final class ManagerRegistry {
    private final CategoryManager categoryManager;
    private final EnchantManager enchantManager;
    private final BookManager bookManager;
    private final EnchantRemoverManager enchantRemoverManager;

    public ManagerRegistry() {
        this.categoryManager = new CategoryManager();
        this.enchantManager = new EnchantManager(categoryManager);
        this.bookManager = new BookManager(enchantManager, categoryManager);
        this.enchantRemoverManager = new EnchantRemoverManager();
    }

    public void reload() {
        categoryManager.reload();
        enchantManager.reload();
        enchantRemoverManager.clearAll();
        BreakBlockAction.reloadIgnoredMaterials();
    }
}