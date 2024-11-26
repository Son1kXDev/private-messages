package com.enjine.privatemessages;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventManager {
    public static void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            PlayerDataManager.PlayerData data = PlayerDataManager.getPlayerData(playerUUID);

            System.out.println("Loaded data for " + player.getEntityName() + ": " + data.ignoredPlayers);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            PlayerDataManager.unloadPlayerData(playerUUID);
        });

    }

    private static Set<ServerPlayerEntity> convertNamesToPlayers(Set<String> playerNames, ServerPlayerEntity currentPlayer) {
        Set<ServerPlayerEntity> players = new HashSet<>();
        for (String name : playerNames) {
            ServerPlayerEntity player = currentPlayer.getServer().getPlayerManager().getPlayer(name);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private static Set<String> convertPlayersToNames(Set<ServerPlayerEntity> players) {
        Set<String> names = new HashSet<>();
        for (ServerPlayerEntity player : players) {
            names.add(player.getEntityName());
        }
        return names;
    }
}
