package com.prox.integration;

import com.prox.integration.entity.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;

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
    public void onDisable() {
        log("Prox Integration disabled");
        eventListener.getMessageInterface().disconnect();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HashMap<String, BaseCommand> commands = new HashMap<>();
        commands.put("settoken", new SetToken(getLogger()));
        commands.put("clearqueue", new ClearQueue(getLogger()));
        commands.put("reconnect", new Reconnect(getLogger()));
        commands.put("dump", new Dump(getLogger()));

        if (!commands.containsKey(command.getName())) {
            return false;
        }

        return commands.get(command.getName()).run(sender, command, label, args, settingsMgr, eventListener);
    }
}
