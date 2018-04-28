/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerChangeStatePacketWrapper;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.network.packet.WrapperShutdownPacketWrapper;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

        Logger.log(getClass(), "["+uuid+"] Registered wrapper");
    }

    public void delete() {
        for (Server s : servers) {
            Logger.log(getClass(), "["+uuid+"] Unregistering server "+s.getInfo().getName());
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
            Logger.log(getClass(), "["+uuid+"] Created server " + s.getInfo().getName() + "!");
        } else {
            Logger.err(getClass(), "["+uuid+"] Cannot create Server because less ram available!");
        }
    }

    public void destroyServer(Server server) {
        this.ramInUse -= server.getInfo().getRam();
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.DELETE));

        server.setWrapper(null);
        server.setState(ServerState.OFFLINE);
        Logger.log(getClass(), "["+uuid+"] Deleted server " + server.getInfo().getName() + "!");
    }

    public void destroyServer(UUID uuid) {
        this.ramInUse -= ram;
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(uuid, ServerChangeStatePacketWrapper.State.DELETE));
    }

    public void startServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.START));
        Logger.log(getClass(), "["+uuid+"] Setting Wrapper Busy...");
        setBusy(true);
        Logger.log(getClass(), "["+uuid+"] Started server " + server.getInfo().getName() + "!");
    }

    public void stopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.STOP));
        Logger.log(getClass(), "["+uuid+"] Stopped server " + server.getInfo().getName() + "!");
    }

    public void forcestopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.FORCESTOP));
        Logger.log(getClass(), "["+uuid+"] Stopped server " + server.getInfo().getName() + "!");
    }

    public void restartServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.RESTART));
        Logger.log(getClass(), "["+uuid+"] Stopped server " + server.getInfo().getName() + "!");
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public String toString() {
        return uuid+" (Connection: "+channel.remoteAddress()+", RAM: "+ram+", Servers: "+servers.size()+")";
    }

}
