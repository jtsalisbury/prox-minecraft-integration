package com.prox.integration.entity;

import com.prox.integration.EventListener;
import com.prox.integration.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Queue;
import java.util.logging.Logger;

public class Dump extends BaseCommand {
    public Dump(Logger logger) {
        super(logger);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args, SettingsManager settings, EventListener eventListener) {
        logger.info("Printing debug information for Prox");

        Queue<String[]> msgQueue = eventListener.getMessageInterface().getMessageQueue();
        logger.info("Messages waiting to be sent: " + msgQueue);

        logger.info("Connection status: " + (eventListener.getMessageInterface().isConnected() ? "connected" : "not connected"));

        sender.sendMessage("Debug information printed to console");

        return true;
    }
}
