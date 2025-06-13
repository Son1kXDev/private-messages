package com.enjine.privatemessages;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class PlayerHistoryManager {
    private static final int MAX = 20;


    public static void addMessage(UUID playerUUID, PlayerDataManager.Message message) {
        PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
        if (data.history.size() == MAX) {
            data.history.remove(0);
        }
        data.history.add(message);
        PlayerDataManager.savePlayerData(playerUUID);
    }

    public static int clear(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {

            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
            if (data.history.isEmpty()) {
                player.sendMessage(Text.of(Text.translatable("private-messages.noHistory").getString()), false);
                return 1;
            }

            data.history.clear();
            PlayerDataManager.savePlayerData(playerUUID);
            player.sendMessage(Text.of(Text.translatable("private-messages.clearHistory").getString()), false);
            return 1;
        }

        return 0;
    }
}
