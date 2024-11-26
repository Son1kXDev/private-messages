package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.enjine.privatemessages.PrivateMessages.config;
import static com.enjine.privatemessages.PrivateMessages.lastMessageSender;

public class MessageHandler {

    public static int sendPrivateMessage(ServerCommandSource source, String targetName, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity target = source.getServer().getPlayerManager().getPlayer(targetName);

        if (target != null) {

            PlayerDataManager.PlayerData senderData = PlayerDataManager.getPlayerData(sender.getUuid());
            PlayerDataManager.PlayerData targetData = PlayerDataManager.getPlayerData(target.getUuid());

            if (senderData.ignoredPlayers.contains(target.getUuid())) {
                sender.sendMessage(Text.literal(config.cannotSendToIgnoredPlayerMessage.replace("{player}", targetName)), false);
                return 0; // Blocked
            }

            if (targetData.ignoredPlayers.contains(sender.getUuid())) {
                sender.sendMessage(Text.literal(config.ignoredByPlayerMessage.replace("{player}", targetName)), false);
                return 0; // Blocked
            }

            lastMessageSender.put(target, source.getPlayer());
            Text receiveMessage = Text.literal(config.receiveMessageFormat
                            .replace("{sender}", source.getName())
                            .replace("{message}", message))
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + source.getName() + " "))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(config.clickToReplyHoverText)))
                            .withColor(Formatting.YELLOW)
                    );
            String sendMessage = config.sendMessageFormat
                    .replace("{target}", targetName)
                    .replace("{message}", message);

            target.sendMessage(receiveMessage, false);
            source.sendMessage(Text.literal(sendMessage));

            if (targetData.notificationEnabled) {
                target.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            }

            return 1; // Success
        } else {
            String playerNotFoundMessage = config.playerNotFoundMessage.replace("{target}", targetName);
            source.sendError(Text.literal(playerNotFoundMessage));
            return 0; // Error
        }
    }

    public static int replyToLastMessage(ServerCommandSource source, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity lastSender = lastMessageSender.get(sender);

        PlayerDataManager.PlayerData lastSenderData = PlayerDataManager.getPlayerData(lastSender.getUuid());

        if (lastSender != null) {
            String receiveMessage = config.receiveMessageFormat
                    .replace("{sender}", sender.getName().getString())
                    .replace("{message}", message);
            String sendMessage = config.sendMessageFormat
                    .replace("{target}", lastSender.getName().getString())
                    .replace("{message}", message);

            lastSender.sendMessage(Text.literal(receiveMessage), false);
            source.sendMessage(Text.literal(sendMessage));

            if (lastSenderData.notificationEnabled) {
                lastSender.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            }

            return 1; // Success
        } else {
            source.sendError(Text.literal(config.noLastMessageError));
            return 0; // Error
        }
    }
}
