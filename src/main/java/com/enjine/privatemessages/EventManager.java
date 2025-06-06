package com.enjine.privatemessages;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                        .replace("{amount}", String.valueOf(data.offlineMessages.size()))).styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm read"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(config.offlineMessageHover)))
                        .withColor(Formatting.YELLOW)
                ));
                if (data.notificationEnabled) {
                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
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
