package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

import static com.enjine.privatemessages.PrivateMessages.*;

public class MessageHandler {

    public static int sendPrivateMessage(ServerCommandSource source, String targetName, String message) {
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

    public static int replyToLastMessage(ServerCommandSource source, String message) {
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
}
