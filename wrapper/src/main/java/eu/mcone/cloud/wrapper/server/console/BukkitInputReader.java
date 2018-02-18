/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.network.packet.ServerProgressStatePacketMaster;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.wrapper.server.Server;

public class BukkitInputReader extends ConsoleInputReader {

    public BukkitInputReader(Server server, boolean outputConsole) {
        super(server, outputConsole);
    }

    @Override
    void filter(String[] lineArray, String line) {
        if (line != null) {
            if (line.contains("! For help, type \"help\" or \"?\"")) {
                //Server is finishing
                String message = String.join(" ", lineArray);
                System.out.println("[" + this.server.getInfo().getName() + " >> " + message);
                this.server.sendProgressState(ServerProgressStatePacketMaster.Progress.NOTPROGRESSING);
                this.server.sendResult("[Bukkit." + server.getInfo().getName() + "] The server started successfully...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            }
        }
    }

}
