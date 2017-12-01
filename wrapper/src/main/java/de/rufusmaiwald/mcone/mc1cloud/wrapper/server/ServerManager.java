/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mcone.mc1cloud.wrapper.server;

import de.rufusmaiwald.mcone.mc1cloud.wrapper.WrapperServer;

import java.util.Timer;
import java.util.TimerTask;

public class ServerManager {

    public ServerManager() {
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Server server : WrapperServer.servers.values()) {
                    int playercount = server.getPlayerCount();
                    String state = server.getState();

                    //Send to master
                    /* ... */
                }
            }
        }, 1000, 5000);
    }

    public static void deleteServer(Server server) {

    }

}
