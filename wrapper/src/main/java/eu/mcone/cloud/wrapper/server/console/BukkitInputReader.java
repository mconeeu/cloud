/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.network.packet.ServerProgressStatePacketMaster;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.wrapper.server.Server;

public class BukkitInputReader extends ConsoleInputReader {

    public BukkitInputReader(Server server, boolean filter) {
        super(server, filter);
    }

    @Override
    void filter(String[] lineArray, String line) {
        if (lineArray[2].equalsIgnoreCase("Done")) {
            if (!(line == null)) {
                //Server is finishing
                String message = String.join(" ", lineArray);
                System.out.println("[" + this.server.getInfo().getName() + " >> " + message);
                this.server.sendProgressState(ServerProgressStatePacketMaster.Progress.NOTPROGRESSING);
                this.server.sendResult("[Server." + server.getInfo().getName() + "] The server started successfully...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            }
        }
    }

}
