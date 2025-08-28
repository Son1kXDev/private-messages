package com.enjine.privatemessages;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

import java.util.Objects;

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

            registerReadOfflineCommand(dispatcher);
            registerHistoryCommand(dispatcher);
            registerHistoryClearCommand(dispatcher);

            registerNotesCommand(dispatcher);
            registerNotesPinCommand(dispatcher);
            registerNotesRemoveCommand(dispatcher);
            registerNotesRemoveRangeCommand(dispatcher);
            registerNotesSearchCommand(dispatcher);
            registerNotesClearCommand(dispatcher);

            registerIgnoreCommand(dispatcher);
            registerNotificationCommand(dispatcher);

            registerHelpCommand(dispatcher);
            registerReloadCommand(dispatcher);
        });
    }

    private static void registerReadOfflineCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("read")
                                .executes(context -> readOfflineMessages(context.getSource()))
                )
        );
    }

    private static void registerHistoryCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("history")
                                .executes(context -> history(context.getSource()))
                )
        );
    }

    private static void registerHistoryClearCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm")
                        .then(CommandManager.literal("history")
                                .then(CommandManager.literal("clear")
                                        .executes(context -> PlayerHistoryManager.clear(context.getSource()))
                                ))
        );
    }

    private static void registerNotesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("notes")
                                .executes(context -> notes(context.getSource(), 1))
                                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                        .executes(
                                                context -> notes(context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "page"))))
                )
        );
    }

    private static void registerNotesClearCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm")
                        .then(CommandManager.literal("notes")
                                .then(CommandManager.literal("clear")
                                        .executes(context -> PlayerNotesManager.clear(context.getSource()))
                                ))

        );
    }

    private static void registerNotesRemoveCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("notes")
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1))
                                                .executes(context ->
                                                        removeNote(
                                                                Objects.requireNonNull(context.getSource().getPlayer()),
                                                                IntegerArgumentType.getInteger(context, "index"),
                                                                context.getSource()
                                                        )
                                                )
                                        )
                                )
                )
        );
    }

    private static void registerNotesRemoveRangeCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("notes")
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("from", IntegerArgumentType.integer(1))
                                                .then(CommandManager.argument("to", IntegerArgumentType.integer(2))
                                                        .executes(context ->
                                                                removeNotes(
                                                                        Objects.requireNonNull(context.getSource().getPlayer()),
                                                                        IntegerArgumentType.getInteger(context, "from"),
                                                                        IntegerArgumentType.getInteger(context, "to"),
                                                                        context.getSource()
                                                                )
                                                        )
                                                )
                                        )
                                )
                )
        );
    }

    private static void registerNotesSearchCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("notes").then(
                                CommandManager.literal("search").then(
                                        CommandManager.argument("keyword", StringArgumentType.string())
                                                .executes(context -> MessageHandler.notes(
                                                                context.getSource(), 1,
                                                                StringArgumentType.getString(context, "keyword")
                                                        )
                                                ).then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(context -> MessageHandler.notes(
                                                                context.getSource(),
                                                                IntegerArgumentType.getInteger(context, "page"),
                                                                StringArgumentType.getString(context, "keyword")
                                                        ))
                                                )
                                )
                        )
                )
        );
    }

    private static void registerNotesPinCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pm").then(
                        CommandManager.literal("notes")
                                .then(CommandManager.literal("pin")
                                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1))
                                                .executes(context -> toggleNotePin(Objects.requireNonNull(context.getSource().getPlayer()),
                                                        IntegerArgumentType.getInteger(context, "index"), context.getSource())))
                                )
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

                                    assert sender != null;
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
                                        .executes(context ->
                                                setNotification(Objects.requireNonNull(context.getSource().getPlayer()), true, context.getSource()))
                                )
                                .then(CommandManager.literal("off")
                                        .executes(context ->
                                                setNotification(Objects.requireNonNull(context.getSource().getPlayer()), false, context.getSource()))
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
                                    source.sendMessage(Text.of("§6======================="));
                                    String helpText = Text.translatable("private-messages.help").getString();
                                    for (String line : helpText.split("\n")) {
                                        source.sendMessage(Text.literal(line));
                                    }
                                    source.sendMessage(Text.of("§6======================="));
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
                                    context.getSource().sendFeedback(() -> Text.translatable("private-messages.reload"), false);
                                    return 1; // Success
                                })
                        )
        );
    }

    private static int removeNote(ServerPlayerEntity player, int index, ServerCommandSource source) {
        int result = PlayerNotesManager.remove(player.getUuid(), index);
        var message = Text.translatable(result == 1 ? "private-messages.noteRemoved" : "private-messages.noteNotFound", index);
        source.sendFeedback(() -> message, false);
        return result;
    }

    private static int removeNotes(ServerPlayerEntity player, int from, int to, ServerCommandSource source) {
        int result = PlayerNotesManager.removeRange(player.getUuid(), from, to);
        var message = Text.translatable(result == 1 ? "private-messages.notesRemoved" : "private-messages.notesNotFound", from, to);
        source.sendFeedback(() -> message, false);
        return result;
    }

    private static int toggleNotePin(ServerPlayerEntity player, int index, ServerCommandSource source) {
        PlayerDataManager.PlayerData playerData = PlayerDataManager.getPlayerData(player.getUuid());
        boolean pinned = PlayerNotesManager.togglePin(player.getUuid(), index);

        var message = Text.translatable(pinned ? "private-messages.notePinned" : "private-messages.noteUnpinned", index);

        source.sendFeedback(() -> message, false);
        return 1;
    }

    private static int toggleIgnorePlayer(ServerPlayerEntity sender, ServerPlayerEntity target, ServerCommandSource source) {
        PlayerDataManager.PlayerData senderData = PlayerDataManager.getPlayerData(sender.getUuid());

        if (senderData.ignoredPlayers.contains(target.getUuid())) {
            senderData.ignoredPlayers.remove(target.getUuid());
            source.sendFeedback(() -> Text.translatable("private-messages.ignoreRemoved", target.getEntityName()), false);
        } else {
            senderData.ignoredPlayers.add(target.getUuid());
            source.sendFeedback(() -> Text.translatable("private-messages.ignoreAdded", target.getEntityName()), false);
        }

        PlayerDataManager.savePlayerData(sender.getUuid());
        return 1; // Success
    }

    private static int setNotification(ServerPlayerEntity player, boolean enabled, ServerCommandSource source) {
        PlayerDataManager.PlayerData playerData = PlayerDataManager.getPlayerData(player.getUuid());
        playerData.notificationEnabled = enabled;
        PlayerDataManager.savePlayerData(player.getUuid());

        var message = Text.translatable(enabled ? "private-messages.notificationEnabled" : "private-messages.notificationDisabled");

        player.playSound(
                enabled ? SoundEvents.BLOCK_NOTE_BLOCK_BELL.value() : SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(),
                SoundCategory.PLAYERS, 1.0F, 1.0F);

        source.sendFeedback(() -> message, false);
        return 1; // Success
    }

}
