package com.prox.integration.entity;

import com.prox.integration.EventListener;
import com.prox.integration.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Queue;
import java.util.logging.Logger;

public class ClearQueue extends BaseCommand {
    public ClearQueue(Logger logger) {
        super(logger);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args, SettingsManager settings, EventListener eventListener) {
        Queue<String[]> msgQueue = eventListener.getMessageInterface().getMessageQueue();

        logger.info("Attempting to remove all outgoing messages in queue");
        logger.info("Number in queue: " + msgQueue.size());

        msgQueue.clear();
        sender.sendMessage("Message queue emptied. Current size: " + msgQueue.size());

        return true;
    }
}
