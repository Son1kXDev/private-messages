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
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import java.util.HashMap;
import java.util.Map;

public class PrivateMessages implements ModInitializer {
    public static final String MOD_ID = "private-messages";
    public static PrivateMessagesConfig config;

    private final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSender = new HashMap<>();
    private final Map<ServerPlayerEntity, Set<ServerPlayerEntity>> ignoredPlayers = new HashMap<>();

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

    private void registerReloadCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("private-messages")
                .then(CommandManager.literal("reload")
                    .executes(context -> {
                        config = ConfigManager.loadConfig();
                        context.getSource().sendFeedback(() -> Text.of("Configuration reloaded."), false);
                        return 1; // Success
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

    private int sendPrivateMessage(ServerCommandSource source, String targetName, String message) {
        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetName);

        if (targetPlayer != null) {
            lastMessageSender.put(targetPlayer, source.getPlayer());
            String receiveMessage = config.receiveMessageFormat
                .replace("{sender}", source.getName())
                .replace("{message}", message);
            String sendMessage = config.sendMessageFormat
                .replace("{target}", targetName)
                .replace("{message}", message);

            targetPlayer.sendMessage(Text.of(receiveMessage), false);
            source.sendMessage(Text.of(sendMessage));
            
            // Play sound to target player
            targetPlayer.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            
            return 1; // Success
        } else {
            String playerNotFoundMessage = config.playerNotFoundMessage.replace("{target}", targetName);
            source.sendError(Text.of(playerNotFoundMessage));
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

            lastSender.sendMessage(Text.of(receiveMessage), false);
            source.sendMessage(Text.of(sendMessage));
            
            // Play sound to the last sender
            lastSender.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            
            return 1; // Success
        } else {
            source.sendError(Text.of(config.noLastMessageError));
            return 0; // Error
        }
    }
}
