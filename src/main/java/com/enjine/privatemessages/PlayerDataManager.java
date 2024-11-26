package com.enjine.privatemessages;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class PlayerDataManager {
    public static Set<String> getIgnoredPlayers(ServerPlayerEntity player) {
        PlayerFileDataManager.PlayerData data = PlayerFileDataManager.loadPlayerData(player);
        return data.ignoredPlayers;
    }

    public static void setIgnoredPlayers(ServerPlayerEntity player, Set<String> ignoredPlayers) {
        PlayerFileDataManager.PlayerData data = PlayerFileDataManager.loadPlayerData(player);
        data.ignoredPlayers = ignoredPlayers;
        PlayerFileDataManager.savePlayerData(player, data);
    }

    public static boolean getNotificationSetting(ServerPlayerEntity player) {
        PlayerFileDataManager.PlayerData data = PlayerFileDataManager.loadPlayerData(player);
        return data.notificationEnabled;
    }

    public static void setNotificationSetting(ServerPlayerEntity player, boolean enabled) {
        PlayerFileDataManager.PlayerData data = PlayerFileDataManager.loadPlayerData(player);
        data.notificationEnabled = enabled;
        PlayerFileDataManager.savePlayerData(player, data);
    }
}
