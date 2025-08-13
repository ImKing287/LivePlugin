# LivePlugin

**LivePlugin** is a plugin for [PaperMC](https://papermc.io) 1.21.8 that lets players register and share their live stream link for Twitch, YouTube, or TikTok, and toggle their live status with a simple command.

## ✨ Features
- Persistent link registration for multiple platforms (`twitch`, `youtube`, `tiktok`).
- Simple commands:
  - `/live register <platform> <link>` – register your live stream link.
  - `/live on <platform>` – set your status to live.
  - `/live off` – stop your live status.
- URL validation (`http://` or `https://`).
- Automatically assigns a configurable LuckPerms group.
- Broadcasts a chat message when a player goes live or stops streaming.
- Player data stored in `players.yml` and kept after server restarts.
- Tab completion for supported platforms.

## ⚙️ Requirements
- PaperMC 1.21.8
- [LuckPerms](https://luckperms.net/) 5.5 or higher

## 📥 Installation
1. Download the latest release of LivePlugin (`LivePlugin.jar`).
2. Place the `.jar` file in your server’s `plugins` folder.
3. Restart the server to generate configuration files.
4. Edit `config.yml` to customize:
   - The LuckPerms group given to live streamers (`live-group`)
   - Chat messages (`messages`)
5. Restart or reload the plugin.

## 📄 Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/live register <platform> <link>` | Registers the link for a platform | `live.use` |
| `/live on <platform>` | Activates live status | `live.use` |
| `/live off` | Deactivates live status | `live.use` |

## 🛠 Default Configuration (`config.yml`)
```yaml
live-group: live
messages:
  prefix: "&6[Live] "
  no-perm: "&cYou do not have permission."
  invalid-platform: "&cInvalid platform! Use: twitch, youtube, tiktok."
  no-link-platform: "&cYou have not registered any link for &e%platform%."
  registered: "&aRegistered link for &e%platform%&a: &e%link%"
  start: "&e%player% &ais now live on &e%platform%: &e%link%"
  stop: "&e%player% &chas stopped streaming."
  help1: "&eUse: /live register <platform> <link>"
  help2: "&eUse: /live on <platform>"
  help3: "&eUse: /live off"
