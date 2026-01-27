package com.mongenscave.mcenchants.executor.action;

import com.mongenscave.mcenchants.executor.action.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ActionRegistry {
    private static final Map<String, EnchantAction> ACTIONS = new HashMap<>();

    static {
        register(new PlantSeedsAction());
        register(new TNTAction());
        register(new PotionAction());
        register(new InvincibleAction());
        register(new WaitAction());
        register(new DoubleDamageAction());
        register(new CancelEventAction());
        register(new AddDurabilityAction());
        register(new ShuffleHotbarAction());
        register(new SmeltAction());
        register(new AddFoodAction());
        register(new AddHealthAction());
        register(new BreakBlockAction());
        register(new CureAction());
        register(new DamageArmorAction());
        register(new DoHarmAction());
        register(new DropHeadAction());
        register(new IncreaseDamageAction());
        register(new LightningAction());
        register(new NegateDamageAction());
        register(new StealExpAction());
        register(new ExpAction());
        register(new KillAction());
        register(new StealHealthAction());
        register(new CurePermanentAction());
        register(new RemoveEnchantAction());
    }

    private static void register(@NotNull EnchantAction action) {
        ACTIONS.put(action.getActionType().toUpperCase(), action);
    }

    @Nullable
    public static EnchantAction getAction(@NotNull String actionType) {
        return ACTIONS.get(actionType.toUpperCase());
    }

    public static boolean isRegistered(@NotNull String actionType) {
        return ACTIONS.containsKey(actionType.toUpperCase());
    }

    @NotNull
    public static Map<String, EnchantAction> getAllActions() {
        return new HashMap<>(ACTIONS);
    }
}