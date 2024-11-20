package com.enjine.privatemessages;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

public class PrivateMessages implements ModInitializer {
    public static final String MOD_ID = "private-messages";
    public static PrivateMessagesConfig config;

    private final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSender = new HashMap<>();
    private final Map<ServerPlayerEntity, Set<ServerPlayerEntity>> ignoredPlayers = new HashMap<>();
    private final Map<ServerPlayerEntity, Boolean> notificationSettings = new HashMap<>();

    @Override
    public void onInitialize() {
        config = ConfigManager.loadConfig();

        // Register commands using CommandRegistrationCallback
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerMessageCommand(dispatcher, "pm");
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

    private void registerMessageCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
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

    private void registerOverwriteMessageCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
        // Check if the command already exists and unregister it
        CommandNode<ServerCommandSource> node = dispatcher.getRoot().getChild(commandName);
        if (node != null) {
            dispatcher.getRoot().getChildren().remove(node);
        }
        // Register the new command
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

    private void registerReplyCommand(CommandDispatcher<ServerCommandSource> dispatcher, String commandName) {
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

    private void registerIgnoreCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
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

    private void registerNotificationCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("private-messages")
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

    private void registerHelpCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("private-messages")
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


    private void registerReloadCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("private-messages")
                .then(CommandManager.literal("reload")
                    .executes(context -> {
                        config = ConfigManager.loadConfig();
                        context.getSource().sendFeedback(() -> Text.literal("Configuration reloaded."), false);
                        return 1; // Success
                    })
                )
        );
    }

    private int sendPrivateMessage(ServerCommandSource source, String targetName, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetName);

        if (targetPlayer != null) {

            Set<ServerPlayerEntity> ignoredSet = ignoredPlayers.getOrDefault(targetPlayer, new HashSet<>());
            if (ignoredSet.contains(sender)) {
                sender.sendMessage(Text.literal(config.cannotSendToIgnoredPlayerMessage.replace("{player}", targetName)), false);
                return 0; // Blocked
            }

            Set<ServerPlayerEntity> senderIgnoredSet = ignoredPlayers.getOrDefault(sender, new HashSet<>());
            if (senderIgnoredSet.contains(targetPlayer)) {
                sender.sendMessage(Text.literal(config.ignoredByPlayerMessage.replace("{player}", targetName)), false);
                return 0; // Blocked
            }

            lastMessageSender.put(targetPlayer, source.getPlayer());
            Text receiveMessage = Text.literal(config.receiveMessageFormat
            .replace("{sender}", source.getName())
            .replace("{message}", message))
            .styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pm " + source.getName() + " "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(config.clickToReplyHoverText)))
                .withColor(Formatting.YELLOW)
            );
            String sendMessage = config.sendMessageFormat
                .replace("{target}", targetName)
                .replace("{message}", message);

            targetPlayer.sendMessage(receiveMessage, false);
            source.sendMessage(Text.literal(sendMessage));
            
            // Play sound to target player
            if (notificationSettings.getOrDefault(targetPlayer, true)) {
                targetPlayer.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            
            return 1; // Success
        } else {
            String playerNotFoundMessage = config.playerNotFoundMessage.replace("{target}", targetName);
            source.sendError(Text.literal(playerNotFoundMessage));
            return 0; // Error
        }
    }

    private int replyToLastMessage(ServerCommandSource source, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity lastSender = lastMessageSender.get(sender);

        if (lastSender != null) {
            String receiveMessage = config.receiveMessageFormat
                .replace("{sender}", sender.getName().getString())
                .replace("{message}", message);
            String sendMessage = config.sendMessageFormat
                .replace("{target}", lastSender.getName().getString())
                .replace("{message}", message);

            lastSender.sendMessage(Text.literal(receiveMessage), false);
            source.sendMessage(Text.literal(sendMessage));
            
            // Play sound to the last sender
            if (notificationSettings.getOrDefault(lastSender, true)) {
                lastSender.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            
            return 1; // Success
        } else {
            source.sendError(Text.literal(config.noLastMessageError));
            return 0; // Error
        }
    }

    private int toggleIgnorePlayer(ServerPlayerEntity sender, ServerPlayerEntity target, ServerCommandSource source) {
        ignoredPlayers.putIfAbsent(sender, new HashSet<>());

        Set<ServerPlayerEntity> ignoredSet = ignoredPlayers.get(sender);
        if (ignoredSet.contains(target)) {
            ignoredSet.remove(target);
            source.sendFeedback(() -> Text.literal(config.ignoreRemovedMessage.replace("{player}", target.getName().getString())), false);
        } else {
            ignoredSet.add(target);
            source.sendFeedback(() -> Text.literal(config.ignoreAddedMessage.replace("{player}", target.getName().getString())), false);
        }
        return 1; // Success
    }

    private int setNotification(ServerPlayerEntity player, boolean enabled, ServerCommandSource source) {
        notificationSettings.put(player, enabled);

        String message = enabled
            ? config.notificationEnabledMessage
            : config.notificationDisabledMessage;

        source.sendFeedback(() -> Text.literal(message), false);
        return 1; // Success
    }
}
