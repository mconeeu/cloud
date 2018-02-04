/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
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

    public Server(ServerInfo info, Template template, String wrapperName) {
        this.info = info;
        this.template = template;
        this.wrapperName = wrapperName;

        //Check if Wrapper is set
        if (this.wrapperName == null) {
            MasterServer.getInstance().getServerManager().addtoServerWaitList(this, null);
        } else {
            Wrapper wrapper = WrapperManager.getWrapperbyString(this.wrapperName);

            //Check if Wrapper exists
            if (wrapper != null) {
                //Create Server on Wrapper
                wrapper.createServer(this);
            } else {
                //Add to ServerWaitingList
                MasterServer.getInstance().getServerManager().addtoServerWaitList(this, this.wrapperName);
            }
        }
    }

    public void start() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.start] No wrapper set for server " + this.info.getName() + ". Adding to ServerWaitList...");
            MasterServer.getInstance().getServerManager().addtoServerWaitList(this, wrapperName);
        } else {
            if (!info.getVersion().equals(ServerVersion.BUNGEE)) {
                for (Server s : MasterServer.getInstance().getServers()) {
                    if (s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                        s.send(new ServerInfoPacket(info));
                    }
                }
            }

            //Start server on Wrapper
            System.out.println("[Server.start] Starting server " + this.info.getName() + "!");
            this.wrapper.startServer(this);
        }
    }

    public void stop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            System.out.println("[Server.stop] No wrapper set for server " + this.info.getName() + ". Adding to ServerWaitList...");
            MasterServer.getInstance().getServerManager().addtoServerWaitList(this, wrapperName);
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
            MasterServer.getInstance().getServerManager().addtoServerWaitList(this, wrapperName);
        } else {
            //Delete Server on Wrapper
            this.wrapper.deleteServer(this);
            this.template.deleteServer(this);
        }
    }

    public void send(Packet packet) {
        if (channel != null) {
            channel.writeAndFlush(packet);
        }
    }

}
