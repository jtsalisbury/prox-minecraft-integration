package com.prox.integration.entity;

import com.prox.integration.EventListener;
import com.prox.integration.MessageInterface;
import com.prox.integration.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class Reconnect extends BaseCommand {
    public Reconnect(Logger logger) {
        super(logger);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args, SettingsManager settings, EventListener eventListener) {
        MessageInterface msgInf = eventListener.getMessageInterface();

        logger.info("Disconnecting current instance... current status: " + msgInf.isConnected());
        msgInf.disconnect();

        logger.info("Attempting to manually reconnect... current status: " + msgInf.isConnected());
        msgInf.connect(eventListener.getToken());

        sender.sendMessage("Connection reset. Check console for connection status");

        return true;
    }
}
