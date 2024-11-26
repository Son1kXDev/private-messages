package com.enjine.privatemessages;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import static com.enjine.privatemessages.MessageHandler.replyToLastMessage;
import static com.enjine.privatemessages.MessageHandler.sendPrivateMessage;
import static com.enjine.privatemessages.PrivateMessages.config;

public class GlobalCommandManager {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerMessageCommand(dispatcher, "t");
            registerMessageCommand(dispatcher, "m");

            registerOverwriteMessageCommand(dispatcher, "w");
            registerOverwriteMessageCommand(dispatcher, "msg");
            registerOverwriteMessageCommand(dispatcher, "tell");

            registerReplyCommand(dispatcher, "reply");
            registerReplyCommand(dispatcher, "r");

            registerIgnoreCommand(dispatcher);
            registerNotificationCommand(dispatcher);

            registerHelpCommand(dispatcher);
            registerReloadCommand(dispatcher);
        });
    }

    private static void registerMessageCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
        dispatcher.register(
                CommandManager.literal(commandName)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String message = StringArgumentType.getString(context, "message");
                                            return sendPrivateMessage(context.getSource(), player.getEntityName(), message);
                                        })
                                )
                        )
        );
    }

    private static void registerOverwriteMessageCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
        CommandNode<ServerCommandSource> node = dispatcher.getRoot().getChild(commandName);
        if (node != null) {
            dispatcher.getRoot().getChildren().remove(node);
        }
        dispatcher.register(
                CommandManager.literal(commandName)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String message = StringArgumentType.getString(context, "message");
                                            return sendPrivateMessage(context.getSource(), player.getEntityName(), message);
                                        })
                                )
                        )
        );
    }

    private static void registerReplyCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
        dispatcher.register(
                CommandManager.literal(commandName)
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String message = StringArgumentType.getString(context, "message");
                                    return replyToLastMessage(context.getSource(), message);
                                })
                        )
        );
    }

    private static void registerIgnoreCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("ignore")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity sender = context.getSource().getPlayer();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                    return toggleIgnorePlayer(sender, target, context.getSource());
                                })
                        )
        );
    }

    private static void registerNotificationCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm")
                        .then(CommandManager.literal("notification")
                                .then(CommandManager.literal("on")
                                        .executes(context -> setNotification(context.getSource().getPlayer(), true, context.getSource()))
                                )
                                .then(CommandManager.literal("off")
                                        .executes(context -> setNotification(context.getSource().getPlayer(), false, context.getSource()))
                                )
                        )
        );
    }

    private static void registerHelpCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm")
                        .then(CommandManager.literal("help")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    for (String line : config.helpMessages) {
                                        source.sendFeedback(() -> Text.literal(line), false);
                                    }
                                    return 1; // Success
                                })
                        )
        );
    }


    private static void registerReloadCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm")
                        .then(CommandManager.literal("reload")
                                .requires((source) -> source.hasPermissionLevel(4))
                                .executes(context -> {
                                    PrivateMessages.config = ConfigManager.loadConfig();
                                    context.getSource().sendFeedback(() -> Text.literal("Configuration reloaded."), false);
                                    return 1; // Success
                                })
                        )
        );
    }

    private static int toggleIgnorePlayer(ServerPlayerEntity sender, ServerPlayerEntity target, ServerCommandSource source) {
        PlayerDataManager.PlayerData senderData = PlayerDataManager.getPlayerData(sender.getUuid());

        if (senderData.ignoredPlayers.contains(target.getUuid())) {
            senderData.ignoredPlayers.remove(target.getUuid());
            source.sendFeedback(() -> Text.literal(config.ignoreRemovedMessage.replace("{player}", target.getEntityName())), false);
        } else {
            senderData.ignoredPlayers.add(target.getUuid());
            source.sendFeedback(() -> Text.literal(config.ignoreAddedMessage.replace("{player}", target.getEntityName())), false);
        }

        PlayerDataManager.savePlayerData(sender.getUuid());
        return 1; // Success
    }

    private static int setNotification(ServerPlayerEntity player, boolean enabled, ServerCommandSource source) {
        PlayerDataManager.PlayerData playerData = PlayerDataManager.getPlayerData(player.getUuid());
        playerData.notificationEnabled = enabled;
        PlayerDataManager.savePlayerData(player.getUuid());

        String message = enabled ? config.notificationEnabledMessage : config.notificationDisabledMessage;

        player.playSound(
                enabled ? SoundEvents.BLOCK_NOTE_BLOCK_BELL.value() : SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(),
                SoundCategory.PLAYERS, 1.0F, 1.0F);

        source.sendFeedback(() -> Text.literal(message), false);
        return 1; // Success
    }

}
