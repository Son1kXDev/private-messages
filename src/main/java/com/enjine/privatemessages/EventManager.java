package com.enjine.privatemessages;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashSet;
import java.util.Set;
import static com.enjine.privatemessages.PrivateMessages.ignoredPlayers;
import static com.enjine.privatemessages.PrivateMessages.notificationSettings;

public class EventManager {
    public static void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerFileDataManager.PlayerData data = PlayerFileDataManager.loadPlayerData(player);
            ignoredPlayers.put(player, convertNamesToPlayers(data.ignoredPlayers, player));
            notificationSettings.put(player, data.notificationEnabled);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            PlayerFileDataManager.PlayerData data = new PlayerFileDataManager.PlayerData();
            data.ignoredPlayers = convertPlayersToNames(ignoredPlayers.getOrDefault(player, new HashSet<>()));
            data.notificationEnabled = notificationSettings.getOrDefault(player, true);

            PlayerFileDataManager.savePlayerData(player, data);
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
