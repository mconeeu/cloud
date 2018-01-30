/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import lombok.Getter;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.UUID;

public class Wrapper {

    @Getter
    private String name;
    @Getter
    private int ram, ramInUse;
    @Getter
    private Channel channel;

    @Getter
    private HashMap<UUID, Server> servers = new HashMap<>();

    public Wrapper(Channel channel, int ram) {
        this.channel = channel;
        this.ram = ram;
        this.name = "Wrapper-"+MasterServer.wrappers.size()+1;

        MasterServer.wrappers.put(this.name, this);
        System.out.println("registered new Wrapper: "+toString());
    }

    public void stop() {
        //Request Wrapper stop
        /* ... */
        for (Server server : this.servers.values()) {
            server.delete();
        }
        System.out.println("[Wrapper.class] Stopped Wrapper " + name + "!");
    }

    public void delete() {
        for (Server server : this.servers.values()) {
            server.delete();
        }
        MasterServer.wrappers.remove(this.name);
    }

    public void createServer(Server server) {
        if (server.getInfo().getRam() + this.ramInUse <= this.ram) {
            this.ramInUse += server.getInfo().getRam();

            //Request server creation
            channel.writeAndFlush(new ServerInfoPacket(server.getInfo()));

            server.setWrapper(this);
            servers.put(server.getInfo().getUuid(), server);
            System.out.println("[Wrapper.class] Created server " + server.getInfo().getName() + " at wrapper " + name + "!");
        } else {
            System.err.println("[Wrapper.class] Cannot create Server because less ram available!");
        }
    }

    public void deleteServer(Server server) {
        this.ramInUse -= server.getInfo().getRam();

        //Request server deletion
        /* ... */
        server.setWrapper(null);
        System.out.println("[Wrapper.class] Deleted server " + server.getInfo().getName() + " from wrapper " + name + "!");
    }

    public void startServer(Server server) {
        //Request server start
        /* ... */
        System.out.println("[Wrapper.class] Started server " + server.getInfo().getName() + " at wrapper " + name + "!");
    }

    public void stopServer(Server server) {
        //Request server stop
        /* ... */
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
