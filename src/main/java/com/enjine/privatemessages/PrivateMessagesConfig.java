package com.enjine.privatemessages;

public class PrivateMessagesConfig {
    public String language = "en_us";
    public String sendMessageFormat = "§6[PM -> §b{target}§6]: §f{message}";
    public String receiveMessageFormat = "§6[PM] §a{sender}§r: §f{message}";
    public String offlineMessageFormat = "§6[PM] §a{sender}§r: §f{message}";
    public String historyMessageFormat = "[{number}] §6[PM§b{target}§6] §a{sender}§r: §f{message}";
    public String notesFormat = "§6[{dateTime}]: §f{content}";
}
