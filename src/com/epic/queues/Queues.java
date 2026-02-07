package com.epic.queues;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Queues extends JavaPlugin {

    private final Map<String, QueueData> queues = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadQueues();
        getLogger().info("Queues enabled");
    }

    private void loadQueues() {
        queues.clear();

        for (String queueName : getConfig().getConfigurationSection("queues").getKeys(false)) {
            int maxPlayers = getConfig().getInt("queues." + queueName + ".maxPlayers");
            int countdown = getConfig().getInt("queues." + queueName + ".countdownSeconds");
            List<String> commands = getConfig().getStringList("queues." + queueName + ".commands");

            queues.put(queueName.toLowerCase(), new QueueData(queueName, maxPlayers, countdown, commands));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("queuejoin")) {
            if (args.length != 1) {
                player.sendMessage("§cUsage: /queuejoin <queue>");
                return true;
            }

            QueueData queue = queues.get(args[0].toLowerCase());
            if (queue == null) {
                player.sendMessage("§cThat queue does not exist.");
                return true;
            }

            queue.join(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("queueleave")) {
            for (QueueData queue : queues.values()) {
                if (queue.leave(player)) {
                    player.sendMessage("§aYou left the queue.");
                    return true;
                }
            }
            player.sendMessage("§cYou are not in a queue.");
            return true;
        }

        return false;
    }

    private class QueueData {
        private final String name;
        private final int maxPlayers;
        private final int countdownSeconds;
        private final List<String> commands;

        private final Set<UUID> players = new HashSet<>();
        private BukkitRunnable task;
        private int timeLeft;

        QueueData(String name, int maxPlayers, int countdownSeconds, List<String> commands) {
            this.name = name;
            this.maxPlayers = maxPlayers;
            this.countdownSeconds = countdownSeconds;
            this.commands = commands;
        }

        void join(Player player) {
            if (players.contains(player.getUniqueId())) {
                player.sendMessage("§cYou are already in this queue.");
                return;
            }

            if (players.size() >= maxPlayers) {
                player.sendMessage("§cThis queue is full.");
                return;
            }

            players.add(player.getUniqueId());
            player.sendMessage("§aJoined queue §e" + name + "§a (" + players.size() + "/" + maxPlayers + ")");

            if (players.size() == 1) {
                startTimer();
            }
        }

        boolean leave(Player player) {
            if (!players.remove(player.getUniqueId())) return false;

            if (players.isEmpty()) {
                resetTimer();
            }
            return true;
        }

        private void startTimer() {
            timeLeft = countdownSeconds;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (players.isEmpty()) {
                        resetTimer();
                        return;
                    }

                    if (timeLeft <= 0) {
                        executeCommands();
                        resetTimer();
                        return;
                    }

                    timeLeft--;
                }
            };

            task.runTaskTimer(Queues.this, 20L, 20L);
        }

        private void resetTimer() {
            if (task != null) task.cancel();
            task = null;
            timeLeft = countdownSeconds;
            players.clear();
        }

        private void executeCommands() {
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                for (String cmd : commands) {
                    String parsed = cmd.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                }
            }
        }
    }
}