package com.enjine.privatemessages;

public class PrivateMessagesConfig {
    public String messageFormat = "§6[PM] §a{sender}§r: §f{message}";

    public String sendMessageFormat = "§6[PM -> §b{target}§6]: §f{message}";
    public String receiveMessageFormat = "§6[PM] §a{sender}§r: §f{message}";

    public String playerNotFoundMessage = "§cPlayer {target} not found.";
    public String noLastMessageError = "§cYou have no one to reply to.";
}
