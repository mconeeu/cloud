/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.console;

import eu.mcone.cloud.wrapper.WrapperServer;

public class CommandExecutor implements eu.mcone.cloud.core.console.CommandExecutor {

    @Override
    public void onCommand(String cmd, String[] args) {
        System.out.println("new command: '"+cmd+"'");
        if (cmd.equalsIgnoreCase("help")) {
            System.out.println("------- [HELP] -------\n" +
                    "help > shows this list\n" +
                    "info > returns some information about the wrapper\n" +
                    "stop > stops the wrapper and all its servers");
        } else if (cmd.equalsIgnoreCase("info")) {
            String localChannelAddress = WrapperServer.getInstance().getChannel().remoteAddress().toString();

            System.out.println("------- [INFO] -------\n" +
                    "Wrapper info:\n" +
                    "local channel address > " + localChannelAddress);
        } else if (cmd.equalsIgnoreCase("stop")) {
            System.out.println("------- [STOP] -------\n" +
                    "Wrapper will shutdown shortly");
            WrapperServer.getInstance().shutdown();
        }
    }

}
