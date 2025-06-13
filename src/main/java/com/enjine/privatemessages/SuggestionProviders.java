package com.enjine.privatemessages;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Set;

public class SuggestionProviders {
    public static final SuggestionProvider<ServerCommandSource> OFFLINE_PLAYER_SUGGESTIONS = (context, builder) -> {
        Set<String> names = PlayerDataManager.getAllKnownPlayerNames();
        return CommandSource.suggestMatching(names, builder);
    };

}
