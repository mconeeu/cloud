/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

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

public class Wrapper {

    @Getter
    private String name;

    @Getter
    private long ram, ramInUse;

    @Getter
    private Channel channel;

    @Getter @Setter
    private boolean progressing = false;

    @Getter @Setter
    private boolean hasBungee = false;

    @Getter
    private List<Server> servers = new ArrayList<>();

    public Wrapper(Channel channel, long ram) {
        this.channel = channel;
        this.ram = ram;
        this.name = "Wrapper-"+MasterServer.getInstance().getWrappers().size()+1;

        MasterServer.getInstance().getWrappers().add(this);
        System.out.println("registered new Wrapper: "+toString());
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
        System.out.println("[Wrapper.class] Destroyed Wrapper " + name + "!");
    }

    public void createServer(Server s) {
        if (s.getInfo().getRam() + this.ramInUse <= this.ram) {
            this.ramInUse += s.getInfo().getRam();

            //Request server creation
            channel.writeAndFlush(new ServerInfoPacket(s.getInfo()));

            s.setWrapper(this);
            servers.add(s);
            System.out.println("[Wrapper.class] Created server " + s.getInfo().getName() + " at wrapper " + name + "!");
        } else {
            System.err.println("[Wrapper.class] Cannot create Server because less ram available!");
        }
    }

    public void deleteServer(Server server) {
        this.ramInUse -= server.getInfo().getRam();
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.DELETE));

        server.setWrapper(null);
        System.out.println("[Wrapper.class] Deleted server " + server.getInfo().getName() + " from wrapper " + name + "!");
    }

    public void startServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.START));
        System.out.println("[Wrapper.class] Started server " + server.getInfo().getName() + " at wrapper " + name + "!");
    }

    public void stopServer(Server server) {
        channel.writeAndFlush(new ServerChangeStatePacketWrapper(server.getInfo().getUuid(), ServerChangeStatePacketWrapper.State.STOP));
        System.out.println("[Wrapper.class] Stopped server " + server.getInfo().getName() + " from wrapper " + name + "!");
    }
    
    public int getServercount() {
        return servers.size();
    }

    @Override
    public String toString() {
        return name+" (Connection: "+channel.remoteAddress()+", RAM: "+ram+")";
    }
}
