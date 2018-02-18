/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.network.packet.ServerProgressStatePacketMaster;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Server;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BungeeInputReader extends ConsoleInputReader {

    public BungeeInputReader(Server server, boolean outputConsole) {
        super(server, outputConsole);
    }

    @Override
    void filter(String[] lineArray, String line) {
        if (line != null) {
            if (line.contains("Listening on ")) {

                //Server is finishing
                //System.out.println("[" + this.server.getInfo().getName() + " >> " + line);

                this.server.sendProgressState(ServerProgressStatePacketMaster.Progress.NOTPROGRESSING);
                this.server.sendResult("[Bungee." + server.getInfo().getName() + "] The bungeecord started successfully...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            }
        }else{
            System.out.println("The Line is null, that's a problem");
        }
    }
}
