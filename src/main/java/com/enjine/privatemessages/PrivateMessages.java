package com.enjine.privatemessages;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PrivateMessages implements ModInitializer {
	public static final String MOD_ID = "private-messages";

	@Override
    public void onInitialize() {
        CommandManager.literal("pm").then(CommandManager.argument("player", StringArgumentType.string())
            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, "player");
                    String message = StringArgumentType.getString(context, "message");
                    ServerPlayerEntity targetPlayer = context.getSource().getServer().getPlayerManager().getPlayer(targetName);

                    if (targetPlayer != null) {
                        targetPlayer.sendMessage(Text.of("[PM] " + context.getSource().getName() + ": " + message));
                        context.getSource().sendMessage(Text.of("[PM -> " + targetName + "]: " + message));
                        return 1; // Success
                    } else {
                        context.getSource().sendError(Text.of("Player not found."));
                        return 0; // Failure
                    }
                })
            )
        );
    }
}