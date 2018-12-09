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
import eu.mcone.networkmanager.api.network.packet.Packet;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Log
public class Wrapper {

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

    public Wrapper(UUID uuid, Channel channel, long ram) {
        this.uuid = uuid;
        this.channel = channel;
        this.ram = ram;
        this.servers = new HashSet<>();

        log.info("["+uuid+"] Registered wrapper");
    }

    public void delete() {
        for (Server s : servers) {
            log.info("["+uuid+"] Unregistering server "+s.getInfo().getName());
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
        channel.writeAndFlush(new WrapperShutdownPacketWrapper());
        delete();
    }

    public void createServer(Server s) {
        if (s.getInfo().getRam() + this.ramInUse <= this.ram) {
            this.ramInUse += s.getInfo().getRam();

            //Request server creation
            channel.writeAndFlush(new ServerInfoPacket(s.getInfo()));

            s.setWrapper(this);
            servers.add(s);
            log.info("["+uuid+"] Created server " + s.getInfo().getName() + "!");
        } else {
            log.info("["+uuid+"] Cannot create Server because less ram available!");
        }
    }

    public void destroyServer(Server server) {
        this.ramInUse -= server.getInfo().getRam();
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.DELETE));

        server.setWrapper(null);
        server.setState(ServerState.OFFLINE);
        log.info("["+uuid+"] Deleted server " + server.getInfo().getName() + "!");
    }

    public void destroyServer(UUID uuid) {
        this.ramInUse -= ram;
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(uuid, ServerChangeStatePacketWrapper.State.DELETE));
    }

    public void startServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.START));
        log.info("["+uuid+"] Setting Wrapper Busy...");
        setBusy(true);
        log.info("["+uuid+"] Initialized start of server " + server.getInfo().getName() + "!");
    }

    public void stopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.STOP));
        log.info("["+uuid+"] Initialized stop of server " + server.getInfo().getName() + "!");
    }

    public void forcestopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.FORCESTOP));
        log.info("["+uuid+"] Initialized force-stop of server " + server.getInfo().getName() + "!");
    }

    public void restartServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.RESTART));
        log.info("["+uuid+"] Initialized restart of server " + server.getInfo().getName() + "!");
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public String toString() {
        return uuid+" (Connection: "+channel.remoteAddress()+", RAM: "+ram+", Servers: "+servers.size()+")";
    }

}
