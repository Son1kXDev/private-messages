package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.UUID;

import static com.enjine.privatemessages.PrivateMessages.config;
import static com.enjine.privatemessages.PrivateMessages.lastMessageSender;

public class MessageHandler {

    public static int sendPrivateMessage(ServerCommandSource source, String targetName, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity target = source.getServer().getPlayerManager().getPlayer(targetName);
        PlayerDataManager.PlayerData senderData = PlayerDataManager.getPlayerData(sender.getUuid());

        if (target != null) {

            PlayerDataManager.PlayerData targetData = PlayerDataManager.getPlayerData(target.getUuid());

            if (senderData.ignoredPlayers.contains(target.getUuid())) {
                sender.sendMessage(Text.translatable("private-messages.ignoringPlayer", targetName), false);
                return 0;
            }

            if (targetData.ignoredPlayers.contains(sender.getUuid())) {
                sender.sendMessage(Text.translatable("private-messages.ignoredByPlayer", targetName), false);
                return 0;
            }

            lastMessageSender.put(target, source.getPlayer());
            Text receiveMessage = Text.literal(config.receiveMessageFormat
                            .replace("{sender}", source.getName())
                            .replace("{message}", message))
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + source.getName() + " "))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.clickToReplyHover")))
                            .withColor(Formatting.YELLOW)
                    );
            String sendMessage = config.sendMessageFormat
                    .replace("{target}", targetName)
                    .replace("{message}", message);

            target.sendMessage(receiveMessage, false);
            source.sendMessage(Text.literal(sendMessage));
            PlayerHistoryManager.addMessage(sender.getUuid(), new PlayerDataManager.Message(source.getName(), targetName, message));
            PlayerHistoryManager.addMessage(target.getUuid(), new PlayerDataManager.Message(source.getName(), targetName, message));

            if (targetData.notificationEnabled) {
                target.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            }

            return 1;
        } else {
            UUID targetUUID = PlayerDataManager.getUUIDByName(targetName);

            if (targetUUID != null) {

                PlayerDataManager.PlayerData targetData = PlayerDataManager.getPlayerData(targetUUID);
                if (senderData.ignoredPlayers.contains(targetUUID)) {
                    sender.sendMessage(Text.translatable("private-messages.ignoringPlayer", targetName), false);
                    return 0;
                }
                if (targetData.ignoredPlayers.contains(sender.getUuid())) {
                    sender.sendMessage(Text.translatable("private-messages.ignoredByPlayer", targetName), false);
                    return 0;
                }
                PlayerDataManager.Message msg = new PlayerDataManager.Message(sender.getName().getString(), targetName, message);
                targetData.offlineMessages.add(msg);
                PlayerDataManager.savePlayerData(targetUUID);
                PlayerHistoryManager.addMessage(sender.getUuid(), msg);
                PlayerHistoryManager.addMessage(targetUUID, msg);
                PlayerDataManager.unloadPlayerData(targetUUID);
                sender.sendMessage(Text.translatable("private-messages.messageToOfflinePlayer", targetName), false);
                return 1;
            } else {
                source.sendError(Text.translatable("private-messages.playerNotFound", targetName));
                return 0;
            }
        }
    }

    public static int readOfflineMessages(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player != null) {
            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
            if (data.offlineMessages.isEmpty()) {
                player.sendMessage(Text.translatable("private-messages.noOfflineMessages"), false);
            }
            for (PlayerDataManager.Message msg : data.offlineMessages) {
                Text message = Text.literal(config.offlineMessageFormat
                        .replace("{sender}", msg.sender)
                        .replace("{message}", msg.message)).styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + msg.sender + " "))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.clickToReplyHover")))
                        .withColor(Formatting.YELLOW)
                );
                player.sendMessage(message, false);
            }
            data.offlineMessages.clear();
            PlayerDataManager.savePlayerData(playerUUID);
            return 1;
        } else return 0;
    }

    public static int history(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player != null) {
            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
            if (data.history.isEmpty()) {
                player.sendMessage(Text.translatable("private-messages.noHistory"), false);
                return 1;
            }
            player.sendMessage(Text.translatable("private-messages.historyTitle"), false);
            for (int i = 0; i < data.history.size(); i++) {
                String message = config.historyMessageFormat
                        .replace("{number}", String.valueOf(i + 1))
                        .replace("{message}", data.history.get(i).message)
                        .replace("{sender}", Objects.equals(data.history.get(i).sender, player.getName().getString()) ? "" : data.history.get(i).sender)
                        .replace("{target}", Objects.equals(data.history.get(i).sender, player.getName().getString()) ? " -> " + data.history.get(i).target : "");

                player.sendMessage(Text.literal(message), false);
            }
            PlayerDataManager.savePlayerData(playerUUID);
            return 1;
        } else return 0;
    }

    public static int replyToLastMessage(ServerCommandSource source, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity lastSender = lastMessageSender.get(sender);
        if (lastSender != null) {
            ServerPlayerEntity target = source.getServer().getPlayerManager().getPlayer(lastSender.getName().getString());
            PlayerDataManager.PlayerData lastSenderData = PlayerDataManager.getPlayerData(lastSender.getUuid());

            if (target != null) {
                String receiveMessage = config.receiveMessageFormat
                        .replace("{sender}", sender.getName().getString())
                        .replace("{message}", message);
                String sendMessage = config.sendMessageFormat
                        .replace("{target}", target.getName().getString())
                        .replace("{message}", message);

                target.sendMessage(Text.literal(receiveMessage).styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + source.getName() + " "))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.clickToReplyHover")))
                        .withColor(Formatting.YELLOW)
                ), false);
                source.sendMessage(Text.literal(sendMessage));
                PlayerHistoryManager.addMessage(sender.getUuid(), new PlayerDataManager.Message(source.getName(), target.getName().getString(), message));
                PlayerHistoryManager.addMessage(target.getUuid(), new PlayerDataManager.Message(source.getName(), target.getName().getString(), message));

                if (lastSenderData.notificationEnabled) {
                    target.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                }

                return 1;
            } else {
                UUID targetUUID = PlayerDataManager.getUUIDByName(lastSender.getName().getString());

                if (targetUUID != null) {

                    PlayerDataManager.PlayerData targetData = PlayerDataManager.getPlayerData(targetUUID);

                    PlayerDataManager.Message msg = new PlayerDataManager.Message(sender.getName().getString(), targetData.name, message);
                    targetData.offlineMessages.add(msg);
                    PlayerDataManager.savePlayerData(targetUUID);
                    PlayerHistoryManager.addMessage(sender.getUuid(), msg);
                    PlayerHistoryManager.addMessage(targetUUID, msg);
                    PlayerDataManager.unloadPlayerData(targetUUID);
                    sender.sendMessage(Text.translatable("private-messages.messageToOfflinePlayer", lastSender.getName().getString()));
                    return 1;
                } else {
                    var e = new RuntimeException("Target UUID not found");
                    source.sendError(Text.of(e.getMessage()));
                    return 0;
                }
            }
        } else {
            source.sendError(Text.translatable("private-messages.noLastMessageError"));
            return 0;
        }
    }
}
