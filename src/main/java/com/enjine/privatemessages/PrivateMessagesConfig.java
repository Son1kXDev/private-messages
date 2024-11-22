package com.enjine.privatemessages;

public class PrivateMessagesConfig {
    public String messageFormat = "§6[PM] §a{sender}§r: §f{message}";

    public String sendMessageFormat = "§6[PM -> §b{target}§6]: §f{message}";
    public String receiveMessageFormat = "§6[PM] §a{sender}§r: §f{message}";

    public String playerNotFoundMessage = "§cPlayer {target} not found.";
    public String noLastMessageError = "§cYou have no one to reply to.";

    public String clickToReplyHoverText = "§eClick to reply!";

    public String ignoreAddedMessage = "§aYou are now ignoring §b{player}.";
    public String ignoreRemovedMessage = "§aYou are no longer ignoring §b{player}.";
    public String ignoredByPlayerMessage = "§cYou cannot message {player}, as they are ignoring you.";
    public String cannotSendToIgnoredPlayerMessage = "§cYou cannot send messages to {player} because you are ignoring them.";

    public String notificationEnabledMessage = "§aNotification sound enabled.";
    public String notificationDisabledMessage = "§cNotification sound disabled.";

    public String[] helpMessages = new String[] {
        "§6/pm help §f- Show this help message",
        "§6/pm notification [on/off] §f- Enable or disable notification sounds",
        "§6/pm <player> <message> §f- Send a private message",
        "§6/reply <message> §f- Reply to the last received message",
        "§6/ignore <player> §f- Ignore or unignore a player"
    };
}
