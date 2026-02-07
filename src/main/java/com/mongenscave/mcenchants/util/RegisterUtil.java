package com.mongenscave.mcenchants.util;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.annotation.Category;
import com.mongenscave.mcenchants.annotation.Enchant;
import com.mongenscave.mcenchants.command.CommandEnchant;
import com.mongenscave.mcenchants.handler.CommandExceptionHandler;
import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import com.mongenscave.mcenchants.listener.*;
import com.mongenscave.mcenchants.suggestion.CategorySuggestionProvider;
import com.mongenscave.mcenchants.suggestion.EnchantSuggestionProvider;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.PluginManager;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.orphan.Orphans;

@UtilityClass
public class RegisterUtil {
    private final McEnchants plugin = McEnchants.getInstance();
    private static EnchantTriggerListener enchantTriggerListener;

    public void registerCommands() {
        var lamp = BukkitLamp.builder(plugin)
                .exceptionHandler(new CommandExceptionHandler())
                .suggestionProviders(registry -> {
                    registry.addProviderForAnnotation(
                            Enchant.class,
                            annotation -> new EnchantSuggestionProvider<>()
                    );
                    registry.addProviderForAnnotation(
                            Category.class,
                            annotation -> new CategorySuggestionProvider<>()
                    );
                })
                .build();

        lamp.register(Orphans.path(ConfigKey.ALIASES.getList().toArray(String[]::new))
                .handler(new CommandEnchant()));
    }

    public void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvents(new MenuListener(), plugin);
        pm.registerEvents(new EnchantApplyListener(), plugin);
        pm.registerEvents(new BookRevealListener(), plugin);
        pm.registerEvents(new EnchantRemoverListener(), plugin);

        enchantTriggerListener = new EnchantTriggerListener();
        pm.registerEvents(enchantTriggerListener, plugin);
    }



    public void unregisterListeners() {
        if (enchantTriggerListener != null) enchantTriggerListener.shutdown();
    }
}