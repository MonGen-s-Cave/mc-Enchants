package com.mongenscave.mcenchants.gui;

import com.mongenscave.mcenchants.data.MenuController;
import org.jetbrains.annotations.NotNull;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;

    public PaginatedMenu(@NotNull MenuController menuController) {
        super(menuController);
    }
}