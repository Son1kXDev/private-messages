package com.enjine.privatemessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {
    private static final File DATA_DIR = new File("world/playerdata/pm");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static void initialize() {
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
    }

    public static PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> {
            File file = new File(DATA_DIR, uuid + ".json");
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    return GSON.fromJson(reader, PlayerData.class);
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
            }
            return new PlayerData();
        });
    }

    public static void savePlayerData(UUID playerUUID) {
        PlayerData data = playerDataMap.get(playerUUID);
        if (data != null) {
            File file = new File(DATA_DIR, playerUUID + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
    }

    public static Set<String> getAllKnownPlayerNames() {
        Set<String> names = new HashSet<>();
        File[] files = DATA_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String uuidStr = file.getName().replace(".json", "");
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerData data = getPlayerData(uuid);
                    if (data.name != null) {
                        names.add(data.name);
                    }
                } catch (IllegalArgumentException e) {
                    e.fillInStackTrace();
                }
            }
        }
        return names;
    }

    public static UUID getUUIDByName(String name) {
        File[] files = DATA_DIR.listFiles((dir, fname) -> fname.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
                    PlayerData data = getPlayerData(uuid);
                    if (data.name != null && data.name.equalsIgnoreCase(name)) {
                        return uuid;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }


    public static void unloadPlayerData(UUID playerUUID) {
        savePlayerData(playerUUID);
        playerDataMap.remove(playerUUID);
    }

    public static class PlayerData {
        public Set<UUID> ignoredPlayers = new HashSet<>();
        public boolean notificationEnabled = true;
        public String name = "";
        public List<OfflineMessage> offlineMessages;
    }

    public static class OfflineMessage {
        public String sender = "";
        public String message = "";

        public OfflineMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        public OfflineMessage() {
        }
    }
}

