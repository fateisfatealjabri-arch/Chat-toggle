package com.example.chattoggle;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatToggleMod implements ModInitializer {
    public static boolean chatEnabled = true;
    public static final Set<UUID> allowedPlayers = new HashSet<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chat")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("disable")
                .executes(context -> {
                    chatEnabled = false;
                    context.getSource().sendFeedback(() -> Text.literal("Chat has been disabled."), true);
                    return 1;
                }))
            .then(CommandManager.literal("enable")
                .executes(context -> {
                    chatEnabled = true;
                    context.getSource().sendFeedback(() -> Text.literal("Chat has been enabled."), true);
                    return 1;
                }))
            .then(CommandManager.literal("permision")
                .then(CommandManager.literal("give")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("allowed", BoolArgumentType.bool())
                            .executes(context -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                boolean allowed = BoolArgumentType.getBool(context, "allowed");

                                if (allowed) {
                                    allowedPlayers.add(target.getUuid());
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("Granted chat permission to " + target.getName().getString() + "."),
                                        true
                                    );
                                } else {
                                    allowedPlayers.remove(target.getUuid());
                                    context.getSource().sendFeedback(
                                        () -> Text.literal("Revoked chat permission from " + target.getName().getString() + "."),
                                        true
                                    );
                                }
                                return 1;
                            })))))
        );
    }
}
