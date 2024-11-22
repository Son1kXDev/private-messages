package com.enjine.privatemessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PlayerFileDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File DATA_DIR = new File("world/playerdata/private_messages");

    static {
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
    }

    public static class PlayerData {
        public Set<String> ignoredPlayers = new HashSet<>();
        public boolean notificationEnabled = true;
    }

    public static PlayerData loadPlayerData(ServerPlayerEntity player) {
        File file = new File(DATA_DIR, player.getUuidAsString() + ".json");
        if (!file.exists()) {
            return new PlayerData();
        }

        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, PlayerData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new PlayerData();
        }
    }

    public static void savePlayerData(ServerPlayerEntity player, PlayerData data) {
        File file = new File(DATA_DIR, player.getUuidAsString() + ".json");

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
