package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class PlayerNotesManager {
    private static final int MAX = 100;


    public static void addNote(UUID playerUUID, PlayerDataManager.Note note) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        if (data.notes.size() == MAX) {
            data.notes.remove(0);
        }
        data.notes.add(note);
        PlayerDataManager.savePlayerData(playerUUID);
    }

    public static List<PlayerDataManager.Note> getNotes(UUID playerUUID) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        return data.notes;
    }

    public static int clear(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {

            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
            if (data.notes.isEmpty()) {
                player.sendMessage(Text.translatable("private-messages.noNotes"), false);
                return 1;
            }

            data.notes.clear();
            PlayerDataManager.savePlayerData(playerUUID);
            player.sendMessage(Text.translatable("private-messages.clearNotes"), false);
            return 1;
        }

        return 0;
    }

    public static int remove(UUID playerUUID, int index) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        if (index < 1 || index >= data.notes.size() - 1) return 0;
        data.notes.remove(index - 1);
        PlayerDataManager.savePlayerData(playerUUID);
        return 1;
    }

    public static int removeRange(UUID playerUUID, int from, int to) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        if (from < 1 || to > data.notes.size() - 1 || from >= to) return 0;
        data.notes.subList(from - 1, to - 1).clear();
        PlayerDataManager.savePlayerData(playerUUID);
        return 1;
    }

    public static int edit(UUID playerUUID, int index, String newText) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        if (index < 0 || index >= data.notes.size()) return 0;
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));
        data.notes.set(index, new PlayerDataManager.Note(dateTime, newText));
        PlayerDataManager.savePlayerData(playerUUID);
        return 1;
    }

    public static List<PlayerDataManager.Note> search(UUID playerUUID, String keyword) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        return data.notes.stream()
                .filter(n -> n.content.toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public static boolean togglePin(UUID playerUUID, int index) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        if (index < 1 || index >= data.notes.size() - 1) return false;
        PlayerDataManager.Note note = data.notes.get(index - 1);
        note.pinned = !note.pinned;
        data.notes.set(index - 1, note);
        PlayerDataManager.savePlayerData(playerUUID);
        return note.pinned;
    }
}
