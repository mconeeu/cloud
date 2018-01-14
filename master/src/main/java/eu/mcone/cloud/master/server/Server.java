/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.master.wrapper.WrapperManager;
import lombok.Getter;

import java.util.UUID;

public class Server {

    @Getter
    private UUID uuid;
    @Getter
    private String name, wrapperName, state;
    @Getter
    private Template template;
    @Getter
    private Wrapper wrapper = null;
    @Getter
    private int templateServerID, ram, port, playercount;

    public Server(UUID uuid, String name, Template template, int templateServerID, int ram, String wrapperName) {
        this.uuid = uuid;
        this.name = name;
        this.template = template;
        this.templateServerID = templateServerID;
        this.ram = ram;
        this.playercount = 0;
        this.wrapperName = wrapperName;

        //Check if Wrapper is set
        if (this.wrapperName == null) {
            ServerManager.addtoServerWaitList(this, null);
        } else {
            Wrapper wrapper = WrapperManager.getWrapperbyString(this.wrapperName);

            //Check if Wrapper exists
            if (wrapper != null) {
                //Create Server on Wrapper
                wrapper.createServer(this);
            } else {
                //Add to ServerWaitingList
                ServerManager.addtoServerWaitList(this, this.wrapperName);
            }
        }
    }

    public void start() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.start] No wrapper set for server " + this.name + ". Adding to ServerWaitList...");
            ServerManager.addtoServerWaitList(this, wrapperName);
        } else {
            //Start server on Wrapper
            System.out.println("[Server.start] Starting server " + this.name + "!");
            this.wrapper.startServer(this);
        }
    }

    public void stop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.stop] No wrapper set for server " + this.name + ". Adding to ServerWaitList...");
            ServerManager.addtoServerWaitList(this, wrapperName);
        } else {
            //Stop server on Wrapper
            System.out.println("[Server.stop] Stopping server " + this.name + "!");
            this.wrapper.stopServer(this);
        }
    }

    public void delete() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.delete] No wrapper set for server " + this.name + ". Adding to ServerWaitList...");
            ServerManager.addtoServerWaitList(this, wrapperName);
        } else {
            //Delete Server on Wrapper
            this.wrapper.deleteServer(this);
            this.template.deleteServer(this);
        }
    }

    public void setWrapper(Wrapper wrapper) {
        //Set new Wrapper
        this.wrapper = wrapper;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPort(int port) {
        //Set server port
        this.port = port;
    }

}
