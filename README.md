# Selectium

A Minecraft Fabric mod for organizing players, with an optional Discord bot for shared player lists, clan rosters, and leaderboards.

## Minecraft mod

Customize player categories, group labels, and nicknames locally, or connect to your community’s bot for shared information.

[Download on Modrinth](https://modrinth.com/mod/selectium-mod)

If your community uses the bot, link the mod with the token it sends you by direct message:

```text
/selectium discord link <token>
```

For more command information, run:

```text
/selectium help
```

## Optional Discord bot setup

Requires **Python 3.12+** and a Discord bot with **Server Members Intent** enabled. Invite it with permissions to view channels, send messages, embed links, and read message history.

### Install

```bash
cd SelectiumBot
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
```

On Windows, activate with `.venv\Scripts\activate`.

### Configure

Create `SelectiumBot/.env` with your token and Discord IDs. Enable Discord’s **Developer Mode** to copy IDs.

```ini
DISCORD_TOKEN=your_bot_token
GUILD_ID=123456789012345678

# Published list channels
GUILD_CHANNEL_ID=123456789012345679
LEADERBOARD_CHANNEL_ID=123456789012345680
ALLY_CHANNEL_ID=123456789012345681
NEUTRAL_CHANNEL_ID=123456789012345682
TARGET_CHANNEL_ID=123456789012345683

# Roster roles: lowest rank first, highest last
ORDERED_ROLE_IDS=111111111111111111,222222222222222222

# Roles eligible for mod tokens
MOD_ROLE_IDS=111111111111111111,222222222222222222

# Roles allowed to manage players
BOT_ROLE_IDS=222222222222222222
```

Each role list requires at least one ID; separate multiple IDs with commas. Keep `.env` private.

### Run

From `SelectiumBot/`, with the virtual environment activated:

```bash
python -m uvicorn src.main:app --host 127.0.0.1 --port 8000
```

Manage the database through the Discord slash commands. A player’s clan must match the Discord server’s name to appear in the roster. Link their Discord ID for role grouping and token delivery; eligible members must allow bot DMs.

Lists refresh every 30 minutes. Back up `data/database.db` to preserve records; logs are saved to `logs/app.log`.

## Self-hosting

Run the bot on an always-on server, ideally with automatic restarts. Expose `/ws` through an HTTPS reverse proxy with WebSocket support, forwarding to `127.0.0.1:8000`.

In the mod’s `DiscordConnection.java`, set `DISCORD_WS_URI` to your endpoint:

```java
private static final URI DISCORD_WS_URI = URI.create("wss://your-domain.example/ws");
```

Rebuild and distribute the resulting JAR from `SelectiumMod/build/libs/`.