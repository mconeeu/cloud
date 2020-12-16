/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.exception.CloudRuntimeException;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerRegisterData;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.BungeeServerListUpdater;
import eu.mcone.cloud.master.template.CloudTemplate;
import eu.mcone.cloud.master.wrapper.CloudWrapper;
import group.onegaming.networkmanager.api.packet.Packet;
import group.onegaming.networkmanager.api.pipeline.FutureListeners;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log
public class CloudServer implements Server {

    @Getter
    private final ServerInfo info;
    @Getter @Setter
    private UUID wrapperUuid;
    @Getter
    private final CloudTemplate template;
    @Getter @Setter
    private CloudWrapper wrapper = null;
    @Getter
    private Map<UUID, String> players;
    @Getter @Setter
    private ServerState state;
    @Getter @Setter
    private Channel channel;
    @Getter @Setter
    private boolean preventStart = false;

    public CloudServer(ServerInfo info, CloudTemplate template, UUID wrapperUuid) {
        this.info = info;
        this.template = template;
        this.players = new HashMap<>();
        this.wrapperUuid = wrapperUuid;
        this.state = ServerState.OFFLINE;

        log.info("["+info.getName()+"] Initialized Server "+info.getName()+" (UUID: "+info.getUuid().toString()+")");
    }

    @Override
    public ChannelFuture start() {
        if (wrapper == null) {
            throw new CloudRuntimeException("No wrapper set!");
        } else {
            log.fine("["+info.getName()+"] Starting server...");
            clearPlayers();
            return this.wrapper.startServer(this);
        }
    }

    @Override
    public ChannelFuture stop() {
        if (wrapper == null) {
            throw new CloudRuntimeException("No wrapper set!");
        } else {
            log.fine("["+info.getName()+"] Stopping server...");
            return this.wrapper.stopServer(this);
        }
    }

    @Override
    public ChannelFuture forcestop() {
        if (wrapper == null) {
            throw new CloudRuntimeException("No wrapper set!");
        } else {
            log.fine("["+info.getName()+"] Forcestopping server...");
            return this.wrapper.forcestopServer(this);
        }
    }

    @Override
    public ChannelFuture restart() {
        if (wrapper == null) {
            throw new CloudRuntimeException("No wrapper set!");
        } else {
            log.fine("["+info.getName()+"] Restarting server...");
            clearPlayers();
            return this.wrapper.restartServer(this);
        }
    }

    @Override
    public void delete() {
        try {
            if (wrapper != null) {
                this.wrapper.deleteServer(this).sync();
                BungeeServerListUpdater.unregisterServerOnAllBungees(this);
            }

            if (template != null) template.deleteServer(this);
            if (info.isStaticServer()) MasterServer.getServer().getStaticServerManager().deleteServer(this);
        } catch (InterruptedException e) {
            throw new CloudRuntimeException("Could not contact Wrapper for deletion", e);
        }
    }

    @Override
    public int getPlayerCount() {
        return players.size();
    }

    public void registerFromPluginData(ServerRegisterData data) {
        this.channel = data.getChannel();
        this.state = data.getPacket().getState();
        this.players = data.getPacket().getPlayers();

        this.info.setHostname(data.getPacket().getHostname());
        this.info.setPort(data.getPacket().getPort());

        if (info.getVersion().equals(ServerVersion.BUNGEE)) {
            BungeeServerListUpdater.registerAllServersOnBungee(this);
        }
    }

    @Override
    public ChannelFuture send(Packet packet) {
        return channel.writeAndFlush(packet).addListener(FutureListeners.FUTURE_LISTENER);
    }

    public void addPlayer(UUID uuid, String name) {
        players.put(uuid, name);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public void clearPlayers() {
        players.clear();
    }

    @Override
    public String toString() {
        return info.getName()+" (UUID: "+info.getUuid()+", Template: "+template.getName()+", State: "+state+", Connection: "+channel.remoteAddress()+")";
    }

}
