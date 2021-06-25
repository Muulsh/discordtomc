package ga.shokokuki.discordtomc;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.util.List;

public class DiscordBot {
    private GatewayDiscordClient gateway;
    private boolean botconnected = true;
    private Discordtomc discordtomc;
    private final OkHttpClient httpClient = new OkHttpClient();

    public DiscordBot(Discordtomc discordtomc) {
        this.discordtomc = discordtomc;
    }

    public void Discord(String token) {
        DiscordClient client = DiscordClient.builder(token).build();
        gateway = client.login().block();
        if (gateway == null) {
            System.out.println("Unable to connect to discord, oh well");
            botconnected = false;
        }
        gateway.getEventDispatcher().on(ReadyEvent.class)
            .subscribe(event -> {
                User self = event.getSelf();
                System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
            });

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();

            final MessageChannel channel = message.getChannel().block();
            String channelid = channel.getRestChannel().getId().asString();
            String msg = message.getContent();

            if ("+setchannel".equals(msg)) {

                discordtomc.getConfig().set("channelid", channelid);
                discordtomc.saveConfig();

                TextChannel channelforwebhook = (TextChannel) channel;

                List<Webhook> webhooks = channelforwebhook.getWebhooks().collectList().block();
                boolean createwebhook = true;
                for (Webhook webhook : webhooks) {
                    if ("discordtomc".equals(webhook.getName().get())) {
                        createwebhook = false;
                    }
                }
                if (createwebhook) {
                    Webhook hook = channelforwebhook.createWebhook(spec -> {
                        spec.setName("discordtomc");
                        spec.setReason("discordtomc webhook");
                    }).block();

                    System.out.println(hook);

                    channel.createMessage("webhook created").block();
                } else {
                    channel.createMessage("webhook already exist").block();
                }

            } else {
                Member sender = message.getAuthorAsMember().block();
                if (sender == null) {
                    return;
                }
                if (!sender.isBot() && channelid.equals(discordtomc.getConfig().get("channelid"))) {
                    String sendername = sender.getDisplayName();
                    int rgbR = sender.getColor().block().getRed();
                    int rgbG = sender.getColor().block().getGreen();
                    int rgbB = sender.getColor().block().getBlue();
                    ChatColor color = ColorUtil.fromRGB(rgbR, rgbG, rgbB);

                    Bukkit.getScheduler().runTaskAsynchronously(discordtomc, new Runnable() {
                        @Override
                        public void run() {
                            discordtomc.sendMinecraftMessage(ChatColor.BLUE+"[D] "+color+sendername+ChatColor.RESET+": "+msg);
                        }
                    });

                }
            }
        });

    }

    public void sendMessage(String username, String message, String uuid) {
        String avatar_url = "https://crafatar.com/avatars/" + uuid;
        TextChannel channel = (TextChannel) gateway.getChannelById(Snowflake.of(discordtomc.getConfig().get("channelid").toString())).block();

        List<Webhook> webhooks = channel.getWebhooks().collectList().block();

        for (Webhook webhook : webhooks) {
            if ("discordtomc".equals(webhook.getName().get())) {
                String webhookid = webhook.getData().id().asString();
                String webhooktoken = webhook.getData().token().get();
                sendmsgwebhook(webhookid, webhooktoken, username, message,avatar_url);
                return;
            }
        }
    }
    public void sendMessage(String username, String message) {
        TextChannel channel = (TextChannel) gateway.getChannelById(Snowflake.of(discordtomc.getConfig().get("channelid").toString())).block();

        List<Webhook> webhooks = channel.getWebhooks().collectList().block();

        for (Webhook webhook : webhooks) {
            if ("discordtomc".equals(webhook.getName().get())) {
                String webhookid = webhook.getData().id().asString();
                String webhooktoken = webhook.getData().token().get();
                sendmsgwebhook(webhookid, webhooktoken, username, message,"");
                return;
            }
        }

    }
    public void exitDiscord() {
        if (botconnected) {
            gateway.logout();
        }
    }
    private void sendmsgwebhook(String webhookid, String webhooktoken, String username, String content, String avatar_url) {

        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("content", content)
                .add("username", username)
                .add("avatar_url", avatar_url)
                .build();

        Request request = new Request.Builder()
                .url("https://discord.com/api/v9/webhooks/"+webhookid+"/"+webhooktoken)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) return;

            // Get response body
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}