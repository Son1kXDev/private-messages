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

import static com.enjine.privatemessages.PrivateMessages.LOGGER;

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
                var translated = Text.literal(Text.translatable("private-messages.hasOfflineMessages", data.offlineMessages.size()).getString());
                player.sendMessage(translated.styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm read"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(Text.translatable("private-messages.offlineMessageHover").getString())))
                        .withColor(Formatting.YELLOW)
                ));
                if (data.notificationEnabled) {
                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }

            LOGGER.info("[PM] Player data loaded for {}", player.getEntityName());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            PlayerDataManager.unloadPlayerData(playerUUID);
            LOGGER.info("[PM] Player data unloaded for {}", player.getEntityName());
        });

    }
}
