package com.prox.integration;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    private final SettingsManager settingsMgr = new SettingsManager(getLogger(), getDataFolder().getPath());
    private final EventListener eventListener = new EventListener(getLogger());

    // Helper functions to log
    private void log(String message) {
        getLogger().info(message);
    }

    @Override
    public void onEnable() {
        settingsMgr.loadSettings();

        // We've loaded settings, now let's set our token and try to connect
        eventListener.setToken(settingsMgr.get("authorization"));

        // Register message listener
        Bukkit.getServer().getPluginManager().registerEvents(eventListener, this);

        log("Prox Integration loaded!");
    }

    @Override
    public void onDisable() { }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verify command
        if (!command.getName().equalsIgnoreCase("settoken")) {
            return false;
        }

        // Verify args
        if (args.length != 1) {
            sender.sendMessage("Usage /settoken [token]");
            return false;
        }

        // Let them know we're going to overwrite existing stuff
        if (settingsMgr.get("authorization") != "") {
            sender.sendMessage("Authorization has already been created - this will overwrite the existing one");
        }

        // Save the authorization token and update the event listener
        settingsMgr.set("authorization", args[0]);
        eventListener.setToken(args[0]);

        sender.sendMessage("Authorization set!");
        return true;
    }
}
