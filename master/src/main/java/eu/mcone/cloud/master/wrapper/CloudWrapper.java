/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.api.wrapper.Wrapper;
import eu.mcone.cloud.core.packet.ServerChangeStatePacketWrapper;
import eu.mcone.cloud.core.packet.ServerInfoPacket;
import eu.mcone.cloud.core.packet.WrapperShutdownPacketWrapper;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import group.onegaming.networkmanager.api.packet.Packet;
import eu.mcone.cloud.master.server.CloudServer;
import eu.mcone.networkmanager.api.packet.Packet;
import eu.mcone.networkmanager.api.pipeline.FutureListeners;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Log
public class CloudWrapper implements Wrapper {

    @Getter
    private UUID uuid;
    @Getter
    private long ram;
    @Getter @Setter
    private long ramInUse;
    @Getter
    private Channel channel;
    @Getter @Setter
    private boolean busy = false;
    @Getter
    private Set<Server> servers;

    public CloudWrapper(UUID uuid, Channel channel, long ram) {
        this.uuid = uuid;
        this.channel = channel;
        this.ram = ram;
        this.servers = new HashSet<>();

        log.info("[" + uuid + "] Registered wrapper");
    }

    public void unregister() {
        for (Server s : servers) {
            CloudServer server = (CloudServer) s;

            log.info("[" + uuid + "] Unregistering server " + s.getInfo().getName());
            server.setWrapper(null);
            server.setChannel(null);
            server.clearPlayers();
            server.setState(ServerState.OFFLINE);
        }

        this.ram = -1;
        this.ramInUse = 0;
        this.channel = null;
        this.busy = false;
        this.servers.clear();

        MasterServer.getServer().unregisterWrapper(this);
    }

    public void shutdown() {
        try {
            log.fine("[" + uuid + "] Shutting down wrapper!");
            send(new WrapperShutdownPacketWrapper()).await();
            unregister();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ChannelFuture createServer(CloudServer s) {
        if (!servers.contains(s)) {
            if (s.getInfo().getRam() + this.ramInUse <= this.ram) {
                this.ramInUse += s.getInfo().getRam();
                s.setWrapper(this);
                servers.add(s);

                log.fine("[" + uuid + "] Creating server " + s.getInfo().getName() + "!");
                return send(new ServerInfoPacket(s.getInfo()));
            } else {
                log.warning("[" + uuid + "] Cannot create Server because less ram available!");
            }
        } else {
            log.severe("[" + uuid + "] Server " + s.getInfo().getName() + " is already known on this Wrapper!");
        }

        return null;
    }

    public ChannelFuture deleteServer(CloudServer s) {
        if (servers.contains(s)) {
            this.ramInUse -= s.getInfo().getRam();
            s.setWrapper(null);
            s.setState(ServerState.OFFLINE);

            log.fine("[" + uuid + "] Deleting server " + s.getInfo().getName() + "!");
            servers.remove(s);
            return send(new ServerChangeStatePacketWrapper(s.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.DELETE));
        } else {
            log.severe("[" + uuid + "] Server " + s.getInfo().getName() + " is not known on this Wrapper!");
            return null;
        }
    }

    public ChannelFuture deleteServer(UUID uuid) {
        return send(new ServerChangeStatePacketWrapper(uuid, ServerChangeStatePacketWrapper.State.DELETE));
    }

    public ChannelFuture startServer(CloudServer server) {
        log.finest("[" + uuid + "] Setting Wrapper Busy...");
        setBusy(true);

        log.fine("[" + uuid + "] Starting server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.START));
    }

    public ChannelFuture stopServer(CloudServer server) {
        log.fine("[" + uuid + "] Stopping server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.STOP));
    }

    public ChannelFuture forcestopServer(CloudServer server) {
        log.fine("[" + uuid + "] Force-stopping server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.FORCESTOP));
    }

    public ChannelFuture restartServer(CloudServer server) {
        log.fine("[" + uuid + "] Restarting server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.RESTART));
    }

    public ChannelFuture send(Packet packet) {
        return channel.writeAndFlush(packet).addListener(FutureListeners.FUTURE_LISTENER);
    }

    @Override
    public String toString() {
        return uuid + " (Connection: " + channel.remoteAddress() + ", RAM: " + ram + ", Servers: " + servers.size() + ")";
    }

}
