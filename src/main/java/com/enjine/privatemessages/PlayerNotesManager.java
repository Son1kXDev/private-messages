package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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

    public static int clear(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {

            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
            if (data.notes.isEmpty()) {
                player.sendMessage(Text.of(Text.translatable("private-messages.noNotes").getString()), false);
                return 1;
            }

            data.notes.clear();
            PlayerDataManager.savePlayerData(playerUUID);
            player.sendMessage(Text.of(Text.translatable("private-messages.clearNotes").getString()), false);
            return 1;
        }

        return 0;
    }
}
