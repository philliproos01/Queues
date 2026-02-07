# Queues

**Queues** is a lightweight queue system plugin for **Spigot / Paper 1.21.11**.  
It allows players to join named queues which automatically execute console commands after a configurable countdown timer.

The plugin is simple, configurable, and designed to work fully out of the box.

---

## Features

- Multiple independent queues
- `/queuejoin <queue>` to join a queue
- `/queueleave` to leave a queue early
- Per-queue configuration:
  - Maximum number of players
  - Countdown duration (seconds)
  - Commands executed when the timer ends
- Countdown starts when the **first player joins**
- Commands only affect players **still in the queue**
- Queue resets automatically after execution
- If all players leave early, the timer is cancelled and reset

---

## Requirements

- **Java 21** (required for Minecraft 1.21.x)
- **Spigot or Paper 1.21.11**
  - Paper is recommended

---

## Installation

1. Build or download `Queues.jar`
2. Place the jar into your server’s `plugins/` folder
3. Start the server once to generate `config.yml`
4. Stop the server and configure your queues
5. Start the server again

---

## Configuration

### config.yml example

```yaml
queues:
  example:
    maxPlayers: 5
    countdownSeconds: 30
    commands:
      - "say Queue finished for %player%"
      - "give %player% diamond 1"

  pvp:
    maxPlayers: 2
    countdownSeconds: 10
    commands:
      - "say %player% is entering PvP"
