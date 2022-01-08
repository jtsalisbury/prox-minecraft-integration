package com.prox.integration;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


import java.util.logging.Logger;

public class EventListener implements Listener {
    private MessageInterface msgInterface = null;
    private String token = null;
    private int playerCount = 0;

    public EventListener(Logger logger) {
        this.msgInterface = new MessageInterface(logger);
    }

    // Set the authorization token for use with the socket
    public void setToken(String token) {
        if (token == null) {
            return;
        }

        this.token = token;

        // No use in connecting if no players are connected
        if (playerCount == 0) {
            return;
        }

        // Token was reset - disconnect and reconnect
        if (msgInterface.getConnected()) {
            msgInterface.disconnect();
        }

        msgInterface.connect(token);
    }

    // Attempt to send a message to discord on player chat
    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent ev) {
        Player ply = ev.getPlayer();
        String message = ev.getMessage();

        // Note: Message interface will queue messages until we reconnect, but I don't want to spam discord with chat messages if this happens
        if (!msgInterface.getConnected()) {
            return;
        }

        msgInterface.sendMessage(ply.getDisplayName(), message, null);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent ev) {
        String message = ev.getDeathMessage();

        // Note: Message interface will queue messages until we reconnect, but I don't want to spam discord with chat messages if this happens
        if (!msgInterface.getConnected()) {
            return;
        }

        msgInterface.sendMessage("Notification", message, "880808");
    }

    // Event for when the player joins
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent ev) {
        playerCount++;

        // First player joined + token is valid
        if (playerCount == 1 && this.token != null) {
            msgInterface.connect(token);
        }

        String playerName = ev.getPlayer().getDisplayName();

        if (playerName.equals("CaptainHunter21")) {
            return;
        }

        msgInterface.sendMessage("Notification", playerName + " has joined", null);
    }

    // Event for when the player leaves
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent ev) {
        playerCount--;

        String playerName = ev.getPlayer().getDisplayName();

        if (!playerName.equals("CaptainHunter21")) {
            msgInterface.sendMessage("Notification", playerName + " has disconnected", null);
        }

        if (playerCount == 0) {
            msgInterface.disconnect();
        }
    }
}
