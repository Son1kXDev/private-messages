package com.enjine.privatemessages;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class PlayerDataManager {
    private static final String IGNORED_PLAYERS_KEY = "ignoredPlayers";
    private static final String NOTIFICATION_SETTINGS_KEY = "notificationSettings";

    public static Set<String> getIgnoredPlayers(ServerPlayerEntity player) {
        NbtCompound playerData = player.getCommandSource().getPlayer().writeNbt(new NbtCompound());
        if (!playerData.contains(IGNORED_PLAYERS_KEY)) {
            return new HashSet<>();
        }

        NbtList ignoredList = playerData.getList(IGNORED_PLAYERS_KEY, 8); // 8 — это тип String
        Set<String> ignoredPlayers = new HashSet<>();
        for (int i = 0; i < ignoredList.size(); i++) {
            ignoredPlayers.add(ignoredList.getString(i));
        }
        return ignoredPlayers;
    }

    public static void setIgnoredPlayers(ServerPlayerEntity player, Set<String> ignoredPlayers) {
        NbtCompound playerData = player.writeNbt(new NbtCompound());
        NbtList ignoredList = new NbtList();
        for (String ignoredPlayer : ignoredPlayers) {
            ignoredList.add(NbtString.of(ignoredPlayer));
        }
        playerData.put(IGNORED_PLAYERS_KEY, ignoredList);
        player.readNbt(playerData); // Применяем изменения
    }

    public static boolean getNotificationSetting(ServerPlayerEntity player) {
        NbtCompound playerData = player.writeNbt(new NbtCompound());
        return playerData.getBoolean(NOTIFICATION_SETTINGS_KEY);
    }

    public static void setNotificationSetting(ServerPlayerEntity player, boolean enabled) {
        NbtCompound playerData = player.writeNbt(new NbtCompound());
        playerData.putBoolean(NOTIFICATION_SETTINGS_KEY, enabled);
        player.readNbt(playerData); // Применяем изменения
    }
}
