# icee's Hub

Discord bot for server rules, support tickets, and custom emojis, stickers, and soundboard sounds.

Set the application's name and the bot's display name to **icee's Hub** in the Discord Developer Portal. Usernames cannot contain spaces, so the unique username can be a short handle such as `icees_hub`.

The bot is a [discord.js](https://discord.js.org/) v14 app. It uses slash commands, buttons, and a modal. All commands and messages are in English.

## Create the Discord application

1. Open the [Discord Developer Portal](https://discord.com/developers/applications) and create a new application.
2. Name it `icee's Hub`.
3. Open **Bot** and reset the token. Copy it into `DISCORD_TOKEN`. Treat the token like a password. Do not commit it, and do not paste it into chat.
4. Leave **Message Content Intent** and **Server Members Intent** off. This bot does not need them.
5. On **General Information**, copy the Application ID into `CLIENT_ID`.

## Invite the bot

Invite the bot before you register commands.

Scopes:

- `bot`
- `applications.commands`

Bot permissions:

- **Manage Roles**, so it can grant the member role when someone taps **I agree**
- **Manage Channels**, so it can open and delete ticket channels
- **View Channels**, **Send Messages**, **Embed Links**, **Attach Files**, and **Read Message History**, so it can post rules, panels, and ticket messages
- **Create Expressions**, so it can add emojis, stickers, and soundboard sounds
- **Manage Expressions**, so it can see who added an expression and remove ones it did not create. Older Discord clients label this **Manage Emojis and Stickers**. It is the same permission.

Replace `YOUR_CLIENT_ID` and open:

```text
https://discord.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&permissions=8797435317264&scope=bot%20applications.commands
```

That permissions number is the bits above combined.

After the bot joins:

1. Turn on Developer Mode in Discord (**User Settings → Advanced**) so you can copy IDs.
2. Create a member role and a separate staff role. Put their IDs in `RULES_ROLE_ID` and `STAFF_ROLE_ID`. Do not use `@everyone`, and do not use the same role for both.
3. Drag the **icee's Hub** role above the member role. Otherwise Discord will not let the bot assign it.

## Configure

From this directory:

```sh
cp .env.example .env
```

Fill in `.env`:

| Variable | Purpose |
| --- | --- |
| `DISCORD_TOKEN` | Bot token |
| `CLIENT_ID` | Application ID |
| `GUILD_ID` | Server where slash commands are registered |
| `RULES_ROLE_ID` | Role granted by the **I agree** button |
| `STAFF_ROLE_ID` | Role that can see tickets and manage expressions |

`.env` stays on your machine. Only `.env.example` is committed, and it has empty values.

Edit [`config/rules.json`](config/rules.json) for the rules text. The file is read every time someone runs `/rules`, so a restart is not required. Run `/rules` again to post the updated message. **I agree** buttons that are already posted keep working.

## Register slash commands

```sh
npm install
npm run register
```

`npm run register` installs the commands on the server in `GUILD_ID`. They show up immediately. The bot must already be in that server. Running it again replaces this bot's commands in that server.

Global registration is optional. Global commands can take up to an hour to appear, and a guild command overrides the global copy in that server:

```sh
npm run register:global
```

Use guild registration while you are setting the bot up.

## Run

```sh
npm start
```

When the bot is connected it prints `icee's Hub is online as ...`. Leave that process running.

If a required variable is missing, the process exits and names it. It does not try to log in with an empty token.

## Commands

`/rules` and `/ticket` can be used by someone with the staff role, or by someone with **Manage Server**. Expression commands can be used by someone with the staff role, or by someone with **Create Expressions** or **Manage Expressions**. Other members can see the commands, and the bot tells them when they are not allowed.

| Command | What it does |
| --- | --- |
| `/rules` | Posts the rules embed and a persistent **I agree** button. You can choose a channel. |
| **I agree** | Grants `RULES_ROLE_ID`. |
| `/ticket` | Posts the ticket panel. You can choose a channel. |
| **Open a ticket** | Asks for a short topic, then opens a private text channel. Each member can have one open ticket. The staff role can see it. |
| **Close ticket** | The opener or a staff member can close it. After a confirmation, the bot deletes the channel in 5 seconds. |
| `/emoji add` | Adds a custom emoji from an attachment or an `https` URL, plus a name. |
| `/emoji list` | Lists this server's custom emojis and marks the ones this bot added. |
| `/emoji remove` | Removes an emoji you choose, including ones the bot did not add. |
| `/sticker add` | Adds a sticker from a file, with a name, description, and tag. |
| `/sticker list` | Lists this server's custom stickers. |
| `/sticker remove` | Removes a sticker you choose. |
| `/soundboard add` | Adds a soundboard sound from an audio file, with a name and a volume from 0 to 100. |
| `/soundboard list` | Lists this server's soundboard sounds. |
| `/soundboard remove` | Removes a sound you choose. |

The sticker tag is a standard emoji or its Discord name, such as `👋` or `wave`. Volume `100` is full volume. Discord stores that as `1`.

## File limits

Discord's server caps depend on the server's Nitro Server Boost level:

| | Base | Level 1 | Level 2 | Level 3 |
| --- | --- | --- | --- | --- |
| Emoji slots | 50 | 100 | 150 | 250 |
| Sticker slots | 5 | 15 | 30 | 60 |
| Soundboard slots | 8 | 24 | 36 | 48 |

Animated emojis have a separate cap. Discord reports that on its own.

| Asset | Formats | Max size | Other limits |
| --- | --- | --- | --- |
| Emoji | PNG, JPEG, GIF | 256 KiB | Name: 2–32 characters, letters, numbers, and underscores |
| Sticker | PNG, APNG, GIF, or Lottie JSON | 512 KiB | Name 2–30 characters, description 2–100, at most 320×320 and 5 seconds. Lottie stickers only work on verified or partnered servers. |
| Sound | MP3 or Ogg | 512 KiB | At most 5.2 seconds. Command volume is 0–100. |

If the server is at its boost limit, the file is too large, the format is wrong, or the bot is missing permission, the bot replies in plain English instead of showing a raw Discord error.

## Checks

Requires Node.js 18.18 or newer.

```sh
npm run typecheck
npm test
```
