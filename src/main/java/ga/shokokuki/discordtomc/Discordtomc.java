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

        String webhookUrl = this.getConfig().getString("webhookUrl");
        String discordToken = this.getConfig().getString("discordToken");

        discordBot = new DiscordBot(this);
        discordBot.Discord(discordToken);
        discordBot.sendMessage("Server", "Server Started");
    }
    @Override
    public void onDisable() {
        discordBot.sendMessage("Server", "Server Closed");
        discordBot.exitDiscord();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String username = event.getPlayer().getName();
        discordBot.sendMessage("Server", username+" joined the game");
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String username = event.getPlayer().getName();
        discordBot.sendMessage("Server", username+" left the game");
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        Player sender = event.getPlayer();
        event.setMessage(msg);
        discordBot.sendMessage(sender.getName(), msg, sender.getUniqueId().toString());
    }
}
