package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.enjine.privatemessages.PrivateMessages.config;
import static com.enjine.privatemessages.PrivateMessages.lastMessageSender;

public class MessageHandler {

    public static int sendPrivateMessage(ServerCommandSource source, String targetName, String message) {
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity target = source.getServer().getPlayerManager().getPlayer(targetName);
        PlayerDataManager.PlayerData senderData = PlayerDataManager.getPlayerData(sender.getUuid());

        if (target != null) {

            if (sender.getUuid() == target.getUuid()) {
                return sendNote(source, message);
            }

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

    public static int sendNote(ServerCommandSource source, String content) {
        ServerPlayerEntity target = source.getPlayer();
        if (target != null) {
            Text sourceMessage = Text.translatable("private-messages.noteSaved", content)
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm notes"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.clickToReadNotesHover")))
                            .withColor(Formatting.YELLOW)
                    );

            source.sendMessage(sourceMessage);
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));
            PlayerNotesManager.addNote(target.getUuid(), new PlayerDataManager.Note(dateTime, content));
            return 1;
        } else return 0;
    }

    public static int notes(ServerCommandSource source, int page) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        UUID playerUUID = player.getUuid();
        List<PlayerDataManager.Note> notes = PlayerNotesManager.getNotes(playerUUID);
        if (notes.isEmpty()) {
            player.sendMessage(Text.translatable("private-messages.noNotes"), false);
            return 1;
        }

        List<Map.Entry<Integer, PlayerDataManager.Note>> indexedNotes = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            indexedNotes.add(new AbstractMap.SimpleEntry<>(i, notes.get(i)));
        }

        indexedNotes.sort((a, b) -> {
            if (a.getValue().pinned && !b.getValue().pinned) return -1;
            if (!a.getValue().pinned && b.getValue().pinned) return 1;
            return 0;
        });

        int notesPerPage = 10;
        int totalPages = (int) Math.ceil((double) indexedNotes.size() / notesPerPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * notesPerPage;
        int endIndex = Math.min(startIndex + notesPerPage, notes.size());
        player.sendMessage(Text.translatable("private-messages.notesTitle"), false);

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<Integer, PlayerDataManager.Note> entry = indexedNotes.get(i);
            int realIndex = entry.getKey();
            PlayerDataManager.Note note = entry.getValue();
            String prefix = note.pinned ? "§e* §f" : "";
            String message = config.notesFormat
                    .replace("{index}", "[" + (realIndex + 1) + "] ")
                    .replace("{dateTime}", note.dateTime)
                    .replace("{content}", note.content);

            player.sendMessage(Text.literal(prefix + message), false);
        }

        MutableText navigation = Text.literal("");

        if (page > 1) {
            navigation.append(
                    Text.literal("§b<-")
                            .setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm notes " + (page - 1))
                            ).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.notesPreviousPage"))
                            ))
            );
        } else {
            navigation.append(Text.literal("§7<-"));
        }

        navigation.append(Text.literal(" §f| " + page + "/" + totalPages + " | "));

        if (page < totalPages) {
            navigation.append(
                    Text.literal("§b->")
                            .setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm notes " + (page + 1))
                            ).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.notesNextPage"))
                            ))
            );
        } else {
            navigation.append(Text.literal("§7->"));
        }

        player.sendMessage(navigation, false);

        PlayerDataManager.savePlayerData(playerUUID);
        return 1;
    }

    public static int notes(ServerCommandSource source, int page, String keyword) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        UUID playerUUID = player.getUuid();
        List<PlayerDataManager.Note> notes = PlayerNotesManager.search(playerUUID, keyword);
        if (notes.isEmpty()) {
            player.sendMessage(Text.translatable("private-messages.notesNotFound"), false);
            return 1;
        }

        int notesPerPage = 10;
        int totalPages = (int) Math.ceil((double) notes.size() / notesPerPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * notesPerPage;
        int endIndex = Math.min(startIndex + notesPerPage, notes.size());
        player.sendMessage(Text.translatable("private-messages.notesSearchTitle"), false);

        for (int i = startIndex; i < endIndex; i++) {
            PlayerDataManager.Note note = notes.get(i);
            String prefix = note.pinned ? "§e[*] " : "";
            String message = config.notesFormat
                    .replace("{index}", "")
                    .replace("{dateTime}", note.dateTime)
                    .replace("{content}", note.content);

            player.sendMessage(Text.literal(prefix + message), false);
        }

        MutableText navigation = Text.literal("");

        if (page > 1) {
            navigation.append(
                    Text.literal("§b<-")
                            .setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/pm notes search \"%s\" %d", keyword, (page - 1)))
                            ).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.notesPreviousPage"))
                            ))
            );
        } else {
            navigation.append(Text.literal("§7<-"));
        }

        navigation.append(Text.literal(" §f| " + page + "/" + totalPages + " | "));

        if (page < totalPages) {
            navigation.append(
                    Text.literal("§b->")
                            .setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/pm notes search \"%s\" %d", keyword, (page + 1)))
                            ).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("private-messages.notesNextPage"))
                            ))
            );
        } else {
            navigation.append(Text.literal("§7->"));
        }

        player.sendMessage(navigation, false);

        PlayerDataManager.savePlayerData(playerUUID);
        return 1;
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
