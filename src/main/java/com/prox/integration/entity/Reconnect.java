package com.prox.integration.entity;

import com.prox.integration.EventListener;
import com.prox.integration.MessageInterface;
import com.prox.integration.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class Reconnect extends BaseCommand {
    public Reconnect(Logger logger) {
        super(logger);
    }

    Integer getNumPlayers() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args, SettingsManager settings, EventListener eventListener) {
        MessageInterface msgInf = eventListener.getMessageInterface();

        logger.info("Disconnecting current instance... current status: " + msgInf.isConnected());
        msgInf.disconnect();

        logger.info("Reconnecting in 15 seconds");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("Attempting to manually reconnect... current status: " + msgInf.isConnected());

                if (getNumPlayers() == 0) {
                    logger.info("Aborting connection, not enough players");
                    return;
                }

                msgInf.connect(eventListener.getToken());
            }
        }, 15 * 1000);

        sender.sendMessage("Connection reset. Reconnecting in 15 seconds");

        return true;
    }
}
