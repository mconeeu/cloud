/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.wrapper.WrapperServer;
import group.onegaming.networkmanager.core.api.console.CommandExecutor;

import java.util.logging.Logger;

public class ConsoleCommandExecutor implements CommandExecutor {

    private final static Logger log = Logger.getLogger("commandExecutor");

    @Override
    public void onCommand(String[] args) {
        log.info("new command: '" + args[0] + "'");
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                log.info("------- [HELP] -------\n" +
                        "help > shows this list\n" +
                        "info > returns some information about the wrapper\n" +
                        "stop > stops the wrapper and all its servers");
            } else if (args[0].equalsIgnoreCase("info")) {
                String localChannelAddress = WrapperServer.getInstance().getChannel().remoteAddress().toString();

                log.info("------- [INFO] -------\n" +
                        "Wrapper info:\n" +
                        "local channel address > " + localChannelAddress);
            } else if (args[0].equalsIgnoreCase("stop")) {
                log.info("------- [STOP] -------\n" +
                        "Wrapper will shutdown shortly");
                WrapperServer.getInstance().shutdown();
            }
        }
    }
}
