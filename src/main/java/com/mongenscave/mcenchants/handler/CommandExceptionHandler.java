package com.mongenscave.mcenchants.handler;

import com.mongenscave.mcenchants.identifier.key.MessageKey;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.node.ParameterNode;

public class CommandExceptionHandler extends BukkitExceptionHandler {
    @Override
    public void onNoPermission(@NotNull NoPermissionException exception, @NotNull BukkitCommandActor actor) {
        actor.error(MessageKey.NO_PERMISSION.getMessage());
    }

    @Override
    public void onSenderNotPlayer(SenderNotPlayerException exception, @NotNull BukkitCommandActor actor) {
        actor.error(MessageKey.PLAYER_REQUIRED.getMessage());
    }

    @Override
    public void onInvalidPlayer(@NotNull InvalidPlayerException exception, @NotNull BukkitCommandActor actor) {
        actor.error(MessageKey.PLAYER_NOT_FOUND.getMessage());
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException exception, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        actor.error(MessageKey.MISSING_ARGUMENT.getMessage());
    }
}