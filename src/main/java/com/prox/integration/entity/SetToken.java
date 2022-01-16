package com.prox.integration.entity;

import com.prox.integration.EventListener;
import com.prox.integration.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class SetToken extends  BaseCommand {
    public SetToken(Logger logger) {
        super(logger);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args, SettingsManager settingsMgr, EventListener eventListener) {
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
