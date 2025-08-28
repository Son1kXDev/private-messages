# Private Messages Mod for Minecraft (Fabric)

**Private Messages** is a Minecraft mod designed to enhance the in-game messaging system with features like private messaging, message replies, ignoring players, notifications and notes. This mod allows players to send direct messages to each other, reply to the last received message, and manage privacy with an ignore list.

## Features
- **Send Private Messages**: Use `/m`, `/t`, `/msg`, `/tell` commands to send private messages to other players.
- **Reply to Last Message**: Quickly reply to the last received private message using `/reply` or `/r`.
- **Reply by Clicking**: You can also reply to a private message simply by clicking on it! Clicking on the message will automatically suggest the reply command with the sender’s name.
- **Notes**: If you send a message to yourself, it will be saved in your personal notes. Use `/pm notes` to manage them.
- **Ignore Players**: Block messages from specific players using the `/ignore` command.
- **Notification Settings**: Enable or disable notification sounds for private messages using `/pm notification [on|off]`.
- **Configurable Settings**: Customize message formats, notification settings, and more through the configuration file.
- **Encryption**: All saved player data is encrypted so that the server administration cannot access it.

## Commands

- `/tell <player> <message>`: Send a private message (if you send it to yourself, the message will be saved in notes.). 
- `/t <player> <message>`: Send a private message (alias for `/tell`).
- `/msg <player> <message>`: Send a private message.
- `/m <player> <message>`: Send a private message (alias for `/msg`).
- `/w <player> <message>`: Send a private message.
- `/reply <message>`: Reply to the last received private message.
- `/r <message>`: Reply to the last received private message (alias for `/reply`).
- `/ignore <player>`: Toggle ignoring a player. You will no longer receive private messages from them.
- `/pm notification [on|off]`: Toggle sound notifications for private messages.
- `/pm notes [page]` - View the list of your notes
- `/pm notes pin <index>` - Pin a note
- `/pm notes remove <index>` - Delete a note
- `/pm notes remove <from> <to>` - Delete notes in the range
- `/pm notes clear` - Delete all notes
- `/pm notes search <keyword>` - Notes search
- `/pm help`: Show a list of all commands and their usage.
- `/pm reload`: Reload the mod's configuration.

## Configuration

The mod allows you to customize various settings by editing the `private-messages.json` file, such as:
- **Language**: Select your language (now supported: English, Russian). If you want more supported languages and can help me with it, create an issue with feature template.
- **Message formats**: Customize how messages appear.
  
### Example Configuration
```json
{
    "language": "en_us",
    "sendMessageFormat": "§6[PM -> §b{target}§6]: §f{message}",
    "receiveMessageFormat": "§6[PM] §a{sender}§r: §f{message}",
    "offlineMessageFormat": "§6[PM] §a{sender}§r: §f{message}",
    "historyMessageFormat": "[{number}] §6[PM§b{target}§6] §a{sender}§r: §f{message}",
    "notesFormat": "{index}§6[{dateTime}]: §f{content}",
}
```
## License
This mod is licensed under the Creative Commons Attribution 4.0 International Public License. See [LICENSE](https://github.com/Son1kXDev/private-messages?tab=License-1-ov-file) for more information.
