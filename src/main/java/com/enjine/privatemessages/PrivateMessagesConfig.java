package com.enjine.privatemessages;

public class PrivateMessagesConfig {
    public String messageFormat = "[PM] {sender}: {message}";

    public String sendMessageFormat = "[PM -> {target}]: {message}";
    public String receiveMessageFormat = "[PM] {sender}: {message}";

    public String playerNotFoundMessage = "Player {target} not found.";
    public String noLastMessageError = "You have no one to reply to.";
}
