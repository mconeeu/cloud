/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.console;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacketWrapper;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.wrapper.Wrapper;

public class CommandExecutor implements eu.mcone.cloud.core.console.CommandExecutor {

    @Override
    public void onCommand(String cmd, String[] args) {
        if (cmd.equalsIgnoreCase("cmd")) {
            if (args.length >= 2) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        if (i != args.length - 1) sb.append(" ");
                    }

                    s.getWrapper().getChannel().writeAndFlush(new ServerCommandExecutePacketWrapper(s.getInfo().getUuid(), sb.toString()));
                    System.out.println("Sent new command '" + sb.toString() + "' to server wrapper...");
                    return;
                }
                Logger.log(getClass(), "no suitable server found for name " + args[0]);
            }
        } else if (cmd.equalsIgnoreCase("startserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.start();
                }
            }
        } else if (cmd.equalsIgnoreCase("stopserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.stop();
                }
            }
        } else if (cmd.equalsIgnoreCase("forcestopserver")) {
            if (args.length == 1) {
                Server s = MasterServer.getInstance().getServer(args[0]);

                if (s != null) {
                    s.forcestop();
                }
            }
        } else if (cmd.equalsIgnoreCase("reload")) {
            if (args.length == 0) {
                MasterServer.getInstance().reload();
            }
        } else if (cmd.equalsIgnoreCase("stop")) {
            if (args.length == 0) {
                Logger.log(getClass(), "------- [STOP] -------\n" +
                        "MasterServer will shutdown shortly");
                MasterServer.getInstance().shutdown();
            } else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
                Logger.log(getClass(), "------- [STOP] -------\n" +
                        "MasterServer and all Wrappers will shutdown shortly");

                Logger.log("Shutdown progress", "Shutting down Wrappers");
                for (Wrapper w : MasterServer.getInstance().getWrappers()) {
                    w.shutdown();
                }

                MasterServer.getInstance().shutdown();
            }
        }
    }

}
