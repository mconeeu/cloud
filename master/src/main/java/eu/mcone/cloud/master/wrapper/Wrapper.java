/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.packet.ServerChangeStatePacketWrapper;
import eu.mcone.cloud.core.packet.ServerInfoPacket;
import eu.mcone.cloud.core.packet.WrapperShutdownPacketWrapper;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import group.onegaming.networkmanager.api.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Log
public class Wrapper {

    public final static GenericFutureListener<Future<? super Void>> FUTURE_LISTENER = future -> {
        if (!future.isSuccess() || future.isCancelled()) {
            log.severe("Netty Flush Operation failed:" +
                    "\nisDone ? " + future.isDone() + ", " +
                    "\nisSuccess ? " + future.isSuccess() + ", " +
                    "\ncause : " + future.cause() + ", " +
                    "\nisCancelled ? " + future.isCancelled());
            if (future.cause() != null) future.cause().printStackTrace();
        }
    };

    @Getter
    private UUID uuid;
    @Getter
    private long ram;
    @Getter
    @Setter
    private long ramInUse;
    @Getter
    private Channel channel;
    @Getter
    @Setter
    private boolean busy = false;
    @Getter
    private Set<Server> servers;

    public Wrapper(UUID uuid, Channel channel, long ram) {
        this.uuid = uuid;
        this.channel = channel;
        this.ram = ram;
        this.servers = new HashSet<>();

        log.info("[" + uuid + "] Registered wrapper");
    }

    public void unregister() {
        for (Server s : servers) {
            log.info("[" + uuid + "] Unregistering server " + s.getInfo().getName());
            s.setWrapper(null);
            s.setChannel(null);
            s.setPlayerCount(-1);
            s.setState(ServerState.OFFLINE);
        }

        this.ram = -1;
        this.ramInUse = 0;
        this.channel = null;
        this.busy = false;
        this.servers.clear();

        MasterServer.getInstance().unregisterWrapper(this);
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

    public ChannelFuture createServer(Server s) {
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

    public ChannelFuture deleteServer(Server s) {
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

    public ChannelFuture startServer(Server server) {
        log.finest("[" + uuid + "] Setting Wrapper Busy...");
        setBusy(true);

        log.fine("[" + uuid + "] Starting server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.START));
    }

    public ChannelFuture stopServer(Server server) {
        log.fine("[" + uuid + "] Stopping server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.STOP));
    }

    public ChannelFuture forcestopServer(Server server) {
        log.fine("[" + uuid + "] Force-stopping server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.FORCESTOP));
    }

    public ChannelFuture restartServer(Server server) {
        log.fine("[" + uuid + "] Restarting server " + server.getInfo().getName() + "!");
        return send(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.RESTART));
    }

    public ChannelFuture send(Packet packet) {
        return channel.writeAndFlush(packet).addListener(FUTURE_LISTENER);
    }

    @Override
    public String toString() {
        return uuid + " (Connection: " + channel.remoteAddress() + ", RAM: " + ram + ", Servers: " + servers.size() + ")";
    }

}
