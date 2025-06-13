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

import static com.enjine.privatemessages.MessageHandler.*;

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

            registerReadOfflineCommand(dispatcher, "read");
            registerIgnoreCommand(dispatcher);
            registerNotificationCommand(dispatcher);

            registerHelpCommand(dispatcher);
            registerReloadCommand(dispatcher);
        });
    }

    private static void registerReadOfflineCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal(commandName)
                                .executes(context -> readOfflineMessages(context.getSource()))
                )
        );
    }

    private static void registerMessageCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
        dispatcher.register(
                CommandManager.literal(commandName)
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .suggests(SuggestionProviders.OFFLINE_PLAYER_SUGGESTIONS)
                                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String targetName = StringArgumentType.getString(context, "player");
                                            String message = StringArgumentType.getString(context, "message");
                                            return sendPrivateMessage(context.getSource(), targetName, message);
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
        registerMessageCommand(dispatcher, commandName);
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
                                    String helpText = Text.translatable("private-messages.help").getString();
                                    for (String line : helpText.split("\n")) {
                                        source.sendMessage(Text.literal(line));
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
                                    context.getSource().sendFeedback(() -> Text.of(Text.translatable("private-messages.reload").getString()), false);
                                    return 1; // Success
                                })
                        )
        );
    }

    private static int toggleIgnorePlayer(ServerPlayerEntity sender, ServerPlayerEntity target, ServerCommandSource source) {
        PlayerDataManager.PlayerData senderData = PlayerDataManager.getPlayerData(sender.getUuid());

        if (senderData.ignoredPlayers.contains(target.getUuid())) {
            senderData.ignoredPlayers.remove(target.getUuid());
            source.sendFeedback(() -> Text.of(Text.translatable("private-messages.ignoreRemoved", target.getEntityName()).getString()), false);
        } else {
            senderData.ignoredPlayers.add(target.getUuid());
            source.sendFeedback(() -> Text.of(Text.translatable("private-messages.ignoreAdded", target.getEntityName()).getString()), false);
        }

        PlayerDataManager.savePlayerData(sender.getUuid());
        return 1; // Success
    }

    private static int setNotification(ServerPlayerEntity player, boolean enabled, ServerCommandSource source) {
        PlayerDataManager.PlayerData playerData = PlayerDataManager.getPlayerData(player.getUuid());
        playerData.notificationEnabled = enabled;
        PlayerDataManager.savePlayerData(player.getUuid());

        var message = Text.of(Text.translatable(enabled ? "private-messages.notificationEnabled" : "private-messages.notificationDisabled").getString());

        player.playSound(
                enabled ? SoundEvents.BLOCK_NOTE_BLOCK_BELL.value() : SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(),
                SoundCategory.PLAYERS, 1.0F, 1.0F);

        source.sendFeedback(() -> message, false);
        return 1; // Success
    }

}
