/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.console;

import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacketWrapper;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.template.Template;

public class CommandExecutor implements eu.mcone.cloud.core.console.CommandExecutor {

    @Override
    public void onCommand(String cmd, String[] args) {
        if (cmd.equalsIgnoreCase("cmd")) {
            if (args.length >= 2) {
                for (Template t : MasterServer.getInstance().getTemplates()) {
                    for (Server s : t.getServers()) {
                        if (s.getInfo().getName().equalsIgnoreCase(args[0])) {
                            StringBuilder sb = new StringBuilder();

                            for (int i = 1; i < args.length; i++) {
                                sb.append(args[i]);
                                if (i != args.length-1) sb.append(" ");
                            }

                            s.getWrapper().getChannel().writeAndFlush(new ServerCommandExecutePacketWrapper(s.getInfo().getUuid(), sb.toString()));
                            System.out.println("Sent new command '" + sb.toString() + "' to server wrapper...");
                            return;
                        }
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase("stop")) {
            if (args.length == 0) {
                System.out.println("------- [STOP] -------\n" +
                        "MasterServer will shutdown shortly");
                MasterServer.getInstance().shutdown();
            }
        }
    }

}
