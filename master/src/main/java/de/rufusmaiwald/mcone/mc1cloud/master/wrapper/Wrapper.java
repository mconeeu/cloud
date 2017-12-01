/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mcone.mc1cloud.master.wrapper;

import de.rufusmaiwald.mcone.mc1cloud.master.MasterServer;
import de.rufusmaiwald.mcone.mc1cloud.master.server.Server;

import java.util.HashMap;
import java.util.UUID;

public class Wrapper {

    private String name;
    private String ip;
    private int port;
    private int ram;
    private int ramInUse;

    private HashMap<UUID, Server> servers = new HashMap<>();

    public Wrapper(String name, String ip,int ram, int port) {
        this.name = name;
        this.ip = ip;
        this.ram = ram;
        this.port = port;

        MasterServer.wrappers.put(this.name, this);
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
        //Request server creation
        /* ... */
        server.setWrapper(this);
        servers.put(server.getUUID(), server);
        System.out.println("[Wrapper.class] Created server " + server.getName() + " at wrapper " + name + "!");
    }

    public void deleteServer(Server server) {
        //Request server deletion
        /* ... */
        server.setWrapper(null);
        System.out.println("[Wrapper.class] Deleted server " + server.getName() + " from wrapper " + name + "!");
    }

    public void startServer(Server server) {
        //Request server start
        /* ... */
        System.out.println("[Wrapper.class] Started server " + server.getName() + " at wrapper " + name + "!");
    }

    public void stopServer(Server server) {
        //Request server stop
        /* ... */
        System.out.println("[Wrapper.class] Stopped server " + server.getName() + " from wrapper " + name + "!");
    }

    
    
    public int getServercount() {
        return servers.size();
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getRam() {
        return ram;
    }

    public int getRamInUse() {
        return ramInUse;
    }
}
