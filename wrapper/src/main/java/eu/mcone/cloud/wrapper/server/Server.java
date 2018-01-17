/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;

import java.util.UUID;

public class Server {

    @Getter
    private UUID uuid;
    @Getter
    private ServerInfo info;

    public Server(ServerInfo info) {
        this.info = info;
        this.uuid = info.getUuid();

        WrapperServer.servers.put(this.uuid, this);
        System.out.println("[Server.class] New Server " + this.info.getName() + " initialized! Creating Directories...");
        /* ... */

        if (this.info.getTemplateName() != null) {
            System.out.println("[Server.class] Downloading template " + this.info.getTemplateName() + " for Server " + this.info.getName() + "...");
            /* ... */
            this.start();
        } else {
            System.out.println("[Server.class] No template set for Server " + this.info.getName() + "! Starting Server...");
            this.start();
        }

        //Send port to master
        /* ... */
    }

    public void start() {
        this.info.setState(ServerState.STARTING);
        /* ... */
        this.info.setState(ServerState.RUNNING);
    }

    public void stop() {
        /* ... */
        this.info.setState(ServerState.STOPPED);
    }

    public void forceStop() {
        /* ... */
        this.info.setState(ServerState.STOPPED);
    }

    public void restart() {
        this.info.setState(ServerState.STOPPED);
        /* ... */
        this.info.setState(ServerState.STARTING);
        /* ... */
        this.info.setState(ServerState.RUNNING);
    }

    public void delete() {
        this.forceStop();
        WrapperServer.servers.remove(this.uuid);
    }



    public int getPlayerCount() {
        /* ... */
        return 0;
    }

}
