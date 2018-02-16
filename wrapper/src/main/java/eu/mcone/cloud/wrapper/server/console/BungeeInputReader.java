/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.network.packet.ServerProgressStatePacketMaster;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.wrapper.server.Server;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BungeeInputReader extends ConsoleInputReader {

    public BungeeInputReader(Server server, boolean filter) {
        super(server, filter);
    }

    @Override
    void filter(String[] lineArray, String line) {
        if (lineArray[2].equalsIgnoreCase("Done")) {
            if (!(line == null)) {
                //Server is finishing
                String message = String.join(" ", lineArray);
                System.out.println("[" + this.server.getInfo().getName() + " >> " + message);
                this.server.sendResult("[Bungee." + server.getInfo().getName() + "] The server started successfully...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            }
        }
    }
}
