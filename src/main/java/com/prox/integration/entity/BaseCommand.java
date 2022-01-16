package com.prox.integration.entity;

import com.prox.integration.EventListener;
import com.prox.integration.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public abstract class BaseCommand {
    public Logger logger;

    public BaseCommand(Logger logger) {
        this.logger = logger;
    }

    public abstract boolean run(CommandSender sender, Command command, String label, String[] args, SettingsManager settings, EventListener eventListener);
}
