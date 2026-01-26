package com.mongenscave.mcenchants;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.mongenscave.mcenchants.config.Config;
import com.mongenscave.mcenchants.manager.ManagerRegistry;
import com.mongenscave.mcenchants.util.RegisterUtil;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.ZapperJavaPlugin;

import java.io.File;

public final class McEnchants extends ZapperJavaPlugin {
    @Getter private static McEnchants instance;
    @Getter private TaskScheduler scheduler;
    @Getter private ManagerRegistry managerRegistry;
    @Getter private Config language;
    @Getter private Config apply;
    @Getter private Config enchants;
    @Getter private Config category;
    @Getter private Config levels;
    @Getter private Config guis;
    private Config config;

    @Override
    public void onLoad() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initializeComponents();

        managerRegistry = new ManagerRegistry();

        RegisterUtil.registerListeners();
        RegisterUtil.registerCommands();
    }

    @Override
    public void onDisable() {}

    public Config getConfiguration() {
        return config;
    }

    private void initializeComponents() {
        final GeneralSettings generalSettings = GeneralSettings.builder()
                .setUseDefaults(false)
                .build();

        final LoaderSettings loaderSettings = LoaderSettings.builder()
                .setAutoUpdate(true)
                .build();

        final UpdaterSettings updaterSettings = UpdaterSettings.builder()
                .setKeepAll(true)
                .setVersioning(new BasicVersioning("version"))
                .build();

        config = loadConfig("config.yml", generalSettings, loaderSettings, updaterSettings);
        language = loadConfig("messages.yml", generalSettings, loaderSettings, updaterSettings);
        guis = loadConfig("guis.yml", generalSettings, loaderSettings, updaterSettings);
        apply = loadConfig("apply.yml", generalSettings, loaderSettings, updaterSettings);
        category = loadConfig("category.yml", generalSettings, loaderSettings, updaterSettings);
        enchants = loadConfig("enchants.yml", generalSettings, loaderSettings, updaterSettings);
        levels = loadConfig("levels.yml", generalSettings, loaderSettings, updaterSettings);
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    private Config loadConfig(@NotNull String fileName, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull UpdaterSettings updaterSettings) {
        return new Config(
                new File(getDataFolder(), fileName),
                getResource(fileName),
                generalSettings,
                loaderSettings,
                DumperSettings.DEFAULT,
                updaterSettings
        );
    }
}