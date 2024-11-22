# Private Messages Mod for Minecraft (Fabric)

**Private Messages** is a Minecraft mod designed to enhance the in-game messaging system with features like private messaging, message replies, ignoring players, and notifications. This mod allows players to send direct messages to each other, reply to the last received message, and manage privacy with an ignore list.

## Features
- **Send Private Messages**: Use `/m`, `/t`, `/msg`, `/tell` commands to send private messages to other players.
- **Reply to Last Message**: Quickly reply to the last received private message using `/reply` or `/r`.
- **Reply by Clicking**: You can also reply to a private message simply by clicking on it! Clicking on the message will automatically suggest the reply command with the sender’s name.
- **Ignore Players**: Block messages from specific players using the `/ignore` command.
- **Notification Settings**: Enable or disable notification sounds for private messages using `/pm notification [on|off]`.
- **Help Command**: Displays a list of commands and their usage with `/pm help`.
- **Configurable Settings**: Customize message formats, notification settings, and more through the configuration file.

## Commands

- `/tell <player> <message>`: Send a private message.
- `/t <player> <message>`: Send a private message (alias for `/tell`).
- `/msg <player> <message>`: Send a private message.
- `/m <player> <message>`: Send a private message (alias for `/msg`).
- `/w <player> <message>`: Send a private message.
- `/reply <message>`: Reply to the last received private message.
- `/r <message>`: Reply to the last received private message (alias for `/reply`).
- `/ignore <player>`: Toggle ignoring a player. You will no longer receive private messages from them.
- `/pm notification [on|off]`: Toggle sound notifications for private messages.
- `/pm help`: Show a list of all commands and their usage.
- `/pm reload`: Reload the mod's configuration.

## Configuration

The mod allows you to customize various settings by editing the `private-messages.json` file, such as:
- **Message formats**: Customize how private messages appear.
- **Notification settings**: Enable or disable sound notifications for received private messages.
- **Help messages**: Edit the text shown in the `/pm help` command.

### Example Configuration
```json
{
  "messageFormat": "§6[PM] §a{sender}§r: §f{message}",
  "sendMessageFormat": "§6[PM -> §b{target}§6]: §f{message}",
  "receiveMessageFormat": "§6[PM] §a{sender}§r: §f{message}",
  "playerNotFoundMessage": "§cPlayer {target} not found.",
  "noLastMessageError": "§cYou have no one to reply to.",
  "clickToReplyHoverText": "§eClick to reply!",
  "ignoreAddedMessage": "§aYou are now ignoring §b{player}.",
  "ignoreRemovedMessage": "§aYou are no longer ignoring §b{player}.",
  "ignoredByPlayerMessage": "§cYou cannot message {player}, as they are ignoring you.",
  "cannotSendToIgnoredPlayerMessage": "§cYou cannot send messages to {player} because you are ignoring them.",
  "notificationEnabledMessage": "§aNotification sound enabled.",
  "notificationDisabledMessage": "§cNotification sound disabled.",
  "helpMessages": [
    "§6/pm help §f- Show this help message",
    "§6/pm notification [on/off] §f- Enable or disable notification sounds",
    "§6/msg <player> <message> §f- Send a private message",
    "§6/reply <message> §f- Reply to the last received message",
    "§6/ignore <player> §f- Ignore or unignore a player"
  ]
}
```
## License
This mod is licensed under the MIT License. See `LICENSE` for more information.
