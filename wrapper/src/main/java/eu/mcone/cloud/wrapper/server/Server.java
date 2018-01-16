/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;

import java.util.UUID;

public class Server {

    @Getter
    private UUID uuid;
    @Getter
    private String name;
    @Getter
    private String templateName, state;
    @Getter
    private int ram, port;

    public Server(UUID uuid, String name, String templateName, int ram, int port) {
        this.uuid = uuid;
        this.name = name;
        this.templateName = templateName;
        this.ram = ram;
        this.port = port;

        WrapperServer.servers.put(this.uuid, this);
        System.out.println("[Server.class] New Server " + this.name + " initialized! Creating Directories...");
        /* ... */

        if (templateName != null) {
            System.out.println("[Server.class] Downloading template " + this.templateName + " for Server " + this.name + "...");
            /* ... */
            this.start();
        } else {
            System.out.println("[Server.class] No template set for Server " + this.name + "! Starting Server...");
            this.start();
        }

        //Send port to master
        /* ... */
    }

    public void start() {
        this.state = "running";

        /* ... */
    }

    public void stop() {
        this.state = "stopped";

        /* ... */
    }

    public void forceStop() {
        this.state = "stopped";

        /* ... */
    }

    public void restart() {
        this.state = "stopped";
        this.state = "started";

        /* ... */
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
