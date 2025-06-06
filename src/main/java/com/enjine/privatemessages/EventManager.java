package com.enjine.privatemessages;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

import static com.enjine.privatemessages.PrivateMessages.config;

public class EventManager {
    public static void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);
            if (data.name.isEmpty()) {
                data.name = player.getName().getString();
                PlayerDataManager.savePlayerData(playerUUID);
            }

            if (!data.offlineMessages.isEmpty()) {
                player.sendMessage(Text.literal(config.offlineMessageTitle
                        .replace("{amount}", String.valueOf(data.offlineMessages.size()))));
                for (PlayerDataManager.OfflineMessage msg : data.offlineMessages) {
                    Text message = Text.literal(config.offlineMessageFormat
                            .replace("{sender}", msg.sender)
                            .replace("{message}", msg.message));
                    player.sendMessage(message, false);
                }
                data.offlineMessages.clear();
                PlayerDataManager.savePlayerData(playerUUID);
            }

            System.out.println("Loaded data for " + player.getEntityName() + ": " + data.ignoredPlayers);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            PlayerDataManager.unloadPlayerData(playerUUID);
        });

    }
}
