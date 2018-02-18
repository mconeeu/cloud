/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.console.Logger;
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
    private ServerState state = ServerState.OFFLINE;
    @Getter @Setter
    private Channel channel;

    public Server(ServerInfo info, Template template, String wrapperName) {
        this.info = info;
        this.template = template;
        this.wrapperName = wrapperName;

        //Check if Wrapper is set
        if (this.wrapperName == null) {
            MasterServer.getInstance().getServerManager().addToServerWaitListIfNotExists(this);
        } else {
            Wrapper wrapper = WrapperManager.getWrapperbyString(this.wrapperName);

            //Check if Wrapper exists
            if (wrapper != null) {
                //Create Server on Wrapper
                wrapper.createServer(this);
            } else {
                //Add to ServerWaitingList
                MasterServer.getInstance().getServerManager().addToServerWaitListIfNotExists(this);
            }
        }
    }

    public void start() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.log(getClass(), "["+info.getName()+"] No wrapper set for server. Adding to ServerWaitList...");
            MasterServer.getInstance().getServerManager().addToServerWaitListIfNotExists(this);
        } else {
            if (!info.getVersion().equals(ServerVersion.BUNGEE)) {
                for (Server s : MasterServer.getInstance().getServers()) {
                    if (s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                        s.send(new ServerInfoPacket(info));
                    }
                }
            }

            //Start server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Starting server...");
            this.wrapper.startServer(this);
        }
    }

    public void stop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.log(getClass(), "["+info.getName()+"] No wrapper set for server. Adding to ServerWaitList...");
            MasterServer.getInstance().getServerManager().addToServerWaitListIfNotExists(this);
        } else {
            //Stop server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Stopping server...");
            this.wrapper.stopServer(this);
        }
    }

    public void forcestop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.log(getClass(), "["+info.getName()+"] No wrapper set for server. Adding to ServerWaitList...");
            MasterServer.getInstance().getServerManager().addToServerWaitListIfNotExists(this);
        } else {
            //Stop server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Forcestopping server...");
            this.wrapper.forcestopServer(this);
        }
    }

    public void restart() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.log(getClass(), "["+info.getName()+"] No wrapper set for server. Adding to ServerWaitList...");
            MasterServer.getInstance().getServerManager().addToServerWaitListIfNotExists(this);
        } else {
            //Stop server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Restarting server...");
            this.wrapper.restartServer(this);
        }
    }

    public void delete() {
        //Delete Server on Wrapper || from ServerWaitList
        if (wrapper != null) {
            this.wrapper.deleteServer(this);
        } else {
            MasterServer.getInstance().getServerManager().removeFromServerWaitList(this);
        }

        this.template.deleteServer(this);
    }

    public void send(Packet packet) {
        if (channel != null) {
            channel.writeAndFlush(packet);
        }
    }

}
