package com.mongenscave.mcenchants.executor.condition;

import com.mongenscave.mcenchants.executor.condition.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConditionRegistry {
    private static final Map<String, EnchantCondition> CONDITIONS = new HashMap<>();

    static {
        register(new SneakingCondition());
        register(new HealthCondition());
        register(new HasWitherCondition());
        register(new HasPoisonCondition());
        register(new HeadshotCondition());
        register(new HoldingWeaponCondition());
    }

    private static void register(@NotNull EnchantCondition condition) {
        CONDITIONS.put(condition.getConditionKey().toLowerCase(), condition);
    }

    @Nullable
    public static EnchantCondition getCondition(@NotNull String key) {
        return CONDITIONS.get(key.toLowerCase());
    }

    public static boolean isRegistered(@NotNull String key) {
        return CONDITIONS.containsKey(key.toLowerCase());
    }

    @NotNull
    public static Map<String, EnchantCondition> getAllConditions() {
        return new HashMap<>(CONDITIONS);
    }
}