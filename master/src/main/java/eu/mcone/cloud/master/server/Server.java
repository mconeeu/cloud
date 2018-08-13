/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerListUpdatePacketPlugin;
import eu.mcone.cloud.core.server.PluginRegisterData;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.core.console.Logger;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Server {

    @Getter
    private ServerInfo info;
    @Getter
    private UUID wrapperUuid;
    @Getter
    private Template template;
    @Getter @Setter
    private Wrapper wrapper = null;
    @Getter @Setter
    private int playerCount = -1;
    @Getter @Setter
    private ServerState state;
    @Getter @Setter
    private Channel channel;
    @Getter @Setter
    private boolean preventStart = false;

    public Server(ServerInfo info, Template template, UUID wrapperUuid) {
        this.info = info;
        this.template = template;
        this.wrapperUuid = wrapperUuid;
        this.state = ServerState.OFFLINE;

        Logger.log(getClass(), "["+info.getName()+"] Initialized Server "+info.getName()+" (UUID: "+info.getUuid().toString()+")");
    }

    public void start() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.err(getClass(), "["+info.getName()+"] No wrapper set for server!");
        } else {
            //Start server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Starting server...");
            this.wrapper.startServer(this);
        }
    }

    public void stop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.err(getClass(), "["+info.getName()+"] No wrapper set for server!");
        } else {
            //Stop server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Stopping server...");
            this.wrapper.stopServer(this);
        }
    }

    public void forcestop() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.err(getClass(), "["+info.getName()+"] No wrapper set for server!");
        } else {
            //Stop server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Forcestopping server...");
            this.wrapper.forcestopServer(this);
        }
    }

    public void restart() {
        //Check if Wrapper is set
        if (wrapper == null) {
            Logger.err(getClass(), "["+info.getName()+"] No wrapper set for server!");
        } else {
            //Stop server on Wrapper
            Logger.log(getClass(), "["+info.getName()+"] Restarting server...");
            this.wrapper.restartServer(this);
        }
    }

    public void delete() {
        if (wrapper != null) {
            this.wrapper.destroyServer(this);
        }

        if (template != null) template.deleteServer(this);
    }

    public void registerPluginData(PluginRegisterData data) {
        this.channel = data.getChannel();
        this.state = data.getPacket().getState();
        this.playerCount = data.getPacket().getPlayercount();

        this.info.setHostname(data.getPacket().getHostname());
        this.info.setPort(data.getPacket().getPort());

        if (info.getVersion().equals(ServerVersion.BUNGEE)) {
            for (Server server : MasterServer.getInstance().getServers()) {
                if (!server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                    Logger.log(getClass(), "["+info.getName()+"] Registering Server "+server.getInfo().getName());
                    send(new ServerListUpdatePacketPlugin(server.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
                }
            }
        }
    }

    public void send(Packet packet) {
        if (channel != null) {
            if (channel.isOpen() && channel.isActive() && channel.isWritable() && channel.isRegistered()) {
                channel.writeAndFlush(packet);
            } else {
                Logger.err(getClass(), "["+info.getName()+"] Could not send Packet "+packet.getClass().getSimpleName()+" (Channel fail)");
            }
        } else {
            Logger.err(getClass(), "["+info.getName()+"] Could not send Packet "+packet.getClass().getSimpleName()+" (Channel == null)");
        }
    }

    @Override
    public String toString() {
        return info.getName()+" (UUID: "+info.getUuid()+", Template: "+template.getName()+", State: "+state+", Connection: "+channel.remoteAddress()+")";
    }

}
