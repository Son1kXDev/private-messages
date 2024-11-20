package com.enjine.privatemessages;

public class PrivateMessagesConfig {
    public String messageFormat = "§6[PM] §a{sender}§r: §f{message}";

    public String sendMessageFormat = "§6[PM -> §b{target}§6]: §f{message}";
    public String receiveMessageFormat = "§6[PM] §a{sender}§r: §f{message}";

    public String playerNotFoundMessage = "§cPlayer {target} not found.";
    public String noLastMessageError = "§cYou have no one to reply to.";

    public String ignoreAddedMessage = "§aYou are now ignoring §b{player}.";
    public String ignoreRemovedMessage = "§aYou are no longer ignoring §b{player}.";
    public String ignoredByPlayerMessage = "§cYou cannot message {target}, as they are ignoring you.";

    public String notificationEnabledMessage = "§aNotification sound enabled.";
    public String notificationDisabledMessage = "§cNotification sound disabled.";
}
