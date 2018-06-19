/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.wrapper.WrapperServer;

public class CommandExecutor implements eu.mcone.cloud.core.console.CommandExecutor {

    @Override
    public void onCommand(String cmd, String[] args) {
        Logger.log(getClass(), "new command: '"+cmd+"'");
        if (cmd.equalsIgnoreCase("help")) {
            Logger.log(getClass(), "------- [HELP] -------\n" +
                                        "help > shows this list\n" +
                                        "info > returns some information about the wrapper\n" +
                                        "stop > stops the wrapper and all its servers");
        } else if (cmd.equalsIgnoreCase("info")) {
            String localChannelAddress = WrapperServer.getInstance().getChannel().remoteAddress().toString();

            Logger.log(getClass(), "------- [INFO] -------\n" +
                                        "Wrapper info:\n" +
                                        "local channel address > " + localChannelAddress);
        } else if (cmd.equalsIgnoreCase("stop")) {
            Logger.log(getClass(), "------- [STOP] -------\n" +
                                        "Wrapper will shutdown shortly");
            WrapperServer.getInstance().shutdown();
        }
    }

}
