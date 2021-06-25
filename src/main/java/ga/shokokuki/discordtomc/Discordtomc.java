package ga.shokokuki.discordtomc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Discordtomc extends JavaPlugin implements Listener {

    public DiscordBot discordBot;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        String discordToken = this.getConfig().getString("discordToken");

        discordBot = new DiscordBot(this);
        discordBot.Discord(discordToken);
        sendDiscordMessage("Server", "Server Started");
    }
    @Override
    public void onDisable() {
        sendDiscordMessage("Server", "Server Closed");
        discordBot.exitDiscord();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String username = event.getPlayer().getName();
        sendDiscordMessage("Server", username+" joined the game");
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String username = event.getPlayer().getName();
        sendDiscordMessage("Server", username+" left the game");
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        Player sender = event.getPlayer();
        event.setMessage(msg);

        sendDiscordMessage(sender.getName(), msg, sender.getUniqueId().toString());

    }
    public void sendDiscordMessage(String username, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                discordBot.sendMessage(username, message);
            }
        });
    }
    public void sendDiscordMessage(String username, String message, String uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                discordBot.sendMessage(username, message, uuid);
            }
        });
    }
    public void sendMinecraftMessage(String message) {
        Bukkit.getServer().broadcastMessage(message);
    }
}
