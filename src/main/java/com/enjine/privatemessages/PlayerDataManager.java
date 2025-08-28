package com.enjine.privatemessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

import static com.enjine.privatemessages.PrivateMessages.LOGGER;

public class PlayerDataManager {
    private static final File DATA_DIR = new File("world/playerdata/pm");
    private static final String ENC_PREFIX = "ENC:";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    public static void initialize() {
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
    }

    public static PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> {
            File file = new File(DATA_DIR, uuid + ".json");
            if (file.exists()) {
                try {
                    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
                    String json;
                    if (content.startsWith(ENC_PREFIX)) {
                        String b64 = content.substring(ENC_PREFIX.length());
                        json = decryptBase64(b64, uuid);
                    } else {
                        json = content;
                    }
                    PlayerData data = GSON.fromJson(json, PlayerData.class);
                    if (data == null) data = new PlayerData();
                    return data;
                } catch (Exception e) {
                    LOGGER.error("Failed to load player data for {}: {}", uuid, e.toString());
                }
            }
            return new PlayerData();
        });
    }

    public static void savePlayerData(UUID playerUUID) {
        PlayerData data = playerDataMap.get(playerUUID);
        if (data != null) {
            File file = new File(DATA_DIR, playerUUID + ".json");
            try {
                String json = GSON.toJson(data);
                String enc = ENC_PREFIX + encryptToBase64(json, playerUUID);
                Files.writeString(file.toPath(), enc, StandardCharsets.UTF_8);
            } catch (Exception e) {
                LOGGER.error("{}", e.getLocalizedMessage());
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
                    PlayerData data = readDataDecrypted(file, uuid);
                    if (data != null && data.name != null) {
                        names.add(data.name);
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.error("{}", e.getLocalizedMessage());
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
                    PlayerData data = readDataDecrypted(file, uuid);
                    if (data != null && data.name != null && data.name.equalsIgnoreCase(name)) {
                        return uuid;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private static PlayerData readDataDecrypted(File file, UUID uuid) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            String json;
            if (content.startsWith(ENC_PREFIX)) {
                String b64 = content.substring(ENC_PREFIX.length());
                json = decryptBase64(b64, uuid);
            } else {
                json = content;
            }
            PlayerData data = GSON.fromJson(json, PlayerData.class);
            if (data == null) data = new PlayerData();
            return data;
        } catch (Exception e) {
            LOGGER.error("Failed to read data from {}: {}", file.getName(), e.toString());
            return null;
        }
    }

    public static void unloadPlayerData(UUID playerUUID) {
        savePlayerData(playerUUID);
        playerDataMap.remove(playerUUID);
    }

    // =====================  CRYPTO  =====================

    private static byte[] getKeyFromUUID(UUID uuid) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(uuid.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String encryptToBase64(String plaintext, UUID uuid) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(getKeyFromUUID(uuid), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        return Base64.getEncoder().encodeToString(out);
    }

    private static String decryptBase64(String base64, UUID uuid) throws Exception {
        byte[] in = Base64.getDecoder().decode(base64);
        if (in.length < IV_BYTES + 16) {
            throw new IllegalArgumentException("Ciphertext too short");
        }
        byte[] iv = Arrays.copyOfRange(in, 0, IV_BYTES);
        byte[] ct = Arrays.copyOfRange(in, IV_BYTES, in.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(getKeyFromUUID(uuid), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, StandardCharsets.UTF_8);
    }

    // =====================  DATA CLASSES  =====================

    public static class PlayerData {
        public Set<UUID> ignoredPlayers = new HashSet<>();
        public boolean notificationEnabled = true;
        public String name = "";
        public List<Message> offlineMessages = new ArrayList<>();
        public List<Message> history = new ArrayList<>();
        public List<Note> notes = new ArrayList<>();
    }

    public static class Message {
        public String sender = "";
        public String target = "";
        public String message = "";

        public Message(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        public Message(String sender, String target, String message) {
            this.sender = sender;
            this.target = target;
            this.message = message;
        }

        public Message() {
        }
    }

    public static class Note {
        public String dateTime = "";
        public String content = "";
        public boolean pinned = false;

        public Note(String dateTime, String content) {
            this.dateTime = dateTime;
            this.content = content;
        }

        public Note() {
        }
    }
}
