/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerChangeStatePacketWrapper;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.network.packet.WrapperShutdownPacketWrapper;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import lombok.Getter;

import io.netty.channel.Channel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Wrapper {

    @Getter
    private String name;
    @Getter
    private long ram, ramInUse;
    @Getter
    private Channel channel;
    @Getter @Setter
    private boolean busy = false;
    @Getter
    private List<Server> servers = new ArrayList<>();

    public Wrapper(Channel channel, long ram) {
        this.channel = channel;
        this.ram = ram;
        this.name = "Wrapper-"+MasterServer.getInstance().getWrappers().size()+1;

        MasterServer.getInstance().getWrappers().add(this);
        Logger.log(getClass(), "["+name+"] Registered wrapper");
    }

    public void shutdown() {
        channel.writeAndFlush(new WrapperShutdownPacketWrapper());
        delete();
    }

    public void delete() {
        for (Server s : servers) {
            s.setWrapper(null);
            s.delete();
        }

        MasterServer.getInstance().getWrappers().remove(this);
        Logger.log(getClass(), "["+name+"] Destroyed Wrapper!");
    }

    public void createServer(Server s) {
        if (s.getInfo().getRam() + this.ramInUse <= this.ram) {
            this.ramInUse += s.getInfo().getRam();

            //Request server creation
            channel.writeAndFlush(new ServerInfoPacket(s.getInfo()));

            s.setWrapper(this);
            servers.add(s);
            Logger.log(getClass(), "["+name+"] Created server " + s.getInfo().getName() + "!");
        } else {
            Logger.err(getClass(), "["+name+"] Cannot create Server because less ram available!");
        }
    }

    public void deleteServer(Server server) {
        this.ramInUse -= server.getInfo().getRam();
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.DELETE));

        server.setWrapper(null);
        Logger.log(getClass(), "["+name+"] Deleted server " + server.getInfo().getName() + "!");
    }

    public void startServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.START));
        Logger.log(getClass(), "["+name+"] Setting Wrapper Busy...");
        setBusy(true);
        Logger.log(getClass(), "["+name+"] Started server " + server.getInfo().getName() + "!");
    }

    public void stopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.STOP));
        Logger.log(getClass(), "["+name+"] Stopped server " + server.getInfo().getName() + "!");
    }

    public void forcestopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.FORCESTOP));
        Logger.log(getClass(), "["+name+"] Stopped server " + server.getInfo().getName() + "!");
    }

    public void restartServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.RESTART));
        Logger.log(getClass(), "["+name+"] Stopped server " + server.getInfo().getName() + "!");
    }
    
    public int getServercount() {
        return servers.size();
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public String toString() {
        return name+" (Connection: "+channel.remoteAddress()+", RAM: "+ram+")";
    }
}
