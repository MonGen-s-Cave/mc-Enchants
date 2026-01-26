package com.mongenscave.mcenchants.util;

import com.mongenscave.mcenchants.McEnchants;
import com.mongenscave.mcenchants.annotation.Enchant;
import com.mongenscave.mcenchants.command.CommandEnchant;
import com.mongenscave.mcenchants.handler.CommandExceptionHandler;
import com.mongenscave.mcenchants.identifier.key.ConfigKey;
import com.mongenscave.mcenchants.listener.MenuListener;
import com.mongenscave.mcenchants.suggestion.EnchantSuggestionProvider;
import lombok.experimental.UtilityClass;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.orphan.Orphans;

@UtilityClass
public class RegisterUtil {
    private final McEnchants plugin = McEnchants.getInstance();

    public void registerCommands() {
        var lamp = BukkitLamp.builder(plugin)
                .exceptionHandler(new CommandExceptionHandler())
                .suggestionProviders(registry -> {
                    registry.addProviderForAnnotation(
                            Enchant.class,
                            annotation -> new EnchantSuggestionProvider<>()
                    );
                })
                .build();

        lamp.register(Orphans.path(ConfigKey.ALIASES.getList().toArray(String[]::new)).handler(new CommandEnchant()));
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new MenuListener(), plugin);
    }
}