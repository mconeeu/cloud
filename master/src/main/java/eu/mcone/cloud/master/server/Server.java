/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.master.wrapper.WrapperManager;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Server {

    @Getter
    private ServerInfo info;
    @Getter
    private String wrapperName;
    @Getter
    private Template template;
    @Getter @Setter
    private Wrapper wrapper = null;
    @Getter @Setter
    private int playerCount = -1;
    @Getter @Setter
    private Channel channel;

    public Server(UUID uuid, String name, Template template, int maxPlayers, int id, int ram, String wrapperName) {
        this.template = template;
        this.wrapperName = wrapperName;

        this.info = new ServerInfo(uuid, name, getTemplateName(), maxPlayers, id, ram);

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
            System.out.println("[Server.start] No wrapper set for server " + this.info.getName() + ". Adding to ServerWaitList...");
            ServerManager.addtoServerWaitList(this, wrapperName);
        } else {
            //Start server on Wrapper
            System.out.println("[Server.start] Starting server " + this.info.getName() + "!");
            this.wrapper.startServer(this);
        }
    }

    public void stop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.stop] No wrapper set for server " + this.info.getName() + ". Adding to ServerWaitList...");
            ServerManager.addtoServerWaitList(this, wrapperName);
        } else {
            //Stop server on Wrapper
            System.out.println("[Server.stop] Stopping server " + this.info.getName() + "!");
            this.wrapper.stopServer(this);
        }
    }

    public void delete() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.delete] No wrapper set for server " + this.info.getName() + ". Adding to ServerWaitList...");
            ServerManager.addtoServerWaitList(this, wrapperName);
        } else {
            //Delete Server on Wrapper
            this.wrapper.deleteServer(this);
            this.template.deleteServer(this);
        }
    }

    public String getTemplateName() {
        if (template != null) return template.getName();
        return null;
    }

    public void setState(ServerState state) {
        this.info.setState(state);
    }

    public void setPort(int port) {
        this.info.setPort(port);
    }

}
