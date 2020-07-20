/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerRegisterData;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.BungeeServerListUpdater;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import group.onegaming.networkmanager.api.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.UUID;

@Log
public class Server {

    @Getter
    private ServerInfo info;
    @Getter
    @Setter
    private UUID wrapperUuid;
    @Getter
    private Template template;
    @Getter
    @Setter
    private Wrapper wrapper = null;
    @Getter
    @Setter
    private int playerCount = -1;
    @Getter
    @Setter
    private ServerState state;
    @Getter
    @Setter
    private Channel channel;
    @Getter
    @Setter
    private boolean preventStart = false;

    public Server(ServerInfo info, Template template, UUID wrapperUuid) {
        this.info = info;
        this.template = template;
        this.wrapperUuid = wrapperUuid;
        this.state = ServerState.OFFLINE;

        log.info("[" + info.getName() + "] Initialized Server " + info.getName() + " (UUID: " + info.getUuid().toString() + ")");
    }

    public void start() {
        if (wrapper == null) {
            log.severe("[" + info.getName() + "] No wrapper set!");
        } else {
            log.fine("[" + info.getName() + "] Starting server...");
            this.wrapper.startServer(this);
        }
    }

    public void stop() {
        if (wrapper == null) {
            log.severe("[" + info.getName() + "] No wrapper set!");
        } else {
            log.fine("[" + info.getName() + "] Stopping server...");
            this.wrapper.stopServer(this);
        }
    }

    public void forcestop() {
        if (wrapper == null) {
            log.severe("[" + info.getName() + "] No wrapper set!");
        } else {
            log.fine("[" + info.getName() + "] Forcestopping server...");
            this.wrapper.forcestopServer(this);
        }
    }

    public void restart() {
        if (wrapper == null) {
            log.severe("[" + info.getName() + "] No wrapper set!");
        } else {
            log.fine("[" + info.getName() + "] Restarting server...");
            this.wrapper.restartServer(this);
        }
    }

    public void delete() {
        if (wrapper != null) {
            this.wrapper.deleteServer(this);
            BungeeServerListUpdater.unregisterServerOnAllBungees(this);
        }

        if (template != null) template.deleteServer(this);
        if (info.isStaticServer()) MasterServer.getInstance().getStaticServerManager().deleteServer(this);
    }

    public void registerFromPluginData(ServerRegisterData data) {
        this.channel = data.getChannel();
        this.state = data.getPacket().getState();
        this.playerCount = data.getPacket().getPlayercount();

        this.info.setHostname(data.getPacket().getHostname());
        this.info.setPort(data.getPacket().getPort());

        if (info.getVersion().equals(ServerVersion.BUNGEE)) {
            BungeeServerListUpdater.registerAllServersOnBungee(this);
        }
    }

    public ChannelFuture send(Packet packet) {
        return channel.writeAndFlush(packet).addListener(Wrapper.FUTURE_LISTENER);
    }

    @Override
    public String toString() {
        return info.getName() + " (UUID: " + info.getUuid() + ", Template: " + template.getName() + ", State: " + state + ", Connection: " + channel.remoteAddress() + ")";
    }

}
