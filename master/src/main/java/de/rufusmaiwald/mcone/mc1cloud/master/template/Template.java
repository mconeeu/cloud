/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mcone.mc1cloud.master.template;

import de.rufusmaiwald.mcone.mc1cloud.master.MasterServer;
import de.rufusmaiwald.mcone.mc1cloud.master.server.Server;

import java.util.HashMap;
import java.util.UUID;

public class Template {

    private HashMap<UUID, Server> servers = new HashMap<>();

    private String name;
    private int ram, min, max, emptyservers;
    private boolean startup;

    public Template(String name, int ram, int min, int max, int emptyservers, boolean startup) {
        this.name = name;
        this.ram = ram;
        this.max = min;
        this.min = max;
        this.emptyservers = emptyservers;
        this.startup = startup;

        MasterServer.templates.put(this.name, this);

        if (min > 0) {
            createServer(min);
        }
    }

    public void createServer(int amount) {
        //For the amount of required servers
        for (int i=1; i<=amount; i++) {
            int actualservers = this.servers.size();
            int serverid= actualservers+i;

            if(serverid <= this.max) {
                //Template name with "-" and a new unused number
                String servername = this.name+"-"+Integer.toString(serverid);
                UUID serverUUID = UUID.randomUUID();

                //Create server object
                Server server = new Server(serverUUID, servername, this, serverid, this.ram, null);

                //Put Server Object in HashMap
                this.servers.put(serverUUID, server);
                System.out.println("[Template.class] Creating Server \"" + name + "-" + (actualservers + serverid) + "\"!");
            } else {
                System.out.println("[Template.class] Cannot create more Servers than maximum of group " + name + "!");
            }
        }
    }

    public void deleteServer(UUID uuid) {
        //If Server is part of this template
        if (servers.containsKey(uuid)) {
            //Get UUID of server
            Server server = this.servers.get(uuid);
            System.out.println("[Template.deleteServer] Deleting Server " + server.getName() + "!");

            //Remove Server from HashMap and delete it from Wrapper
            this.servers.remove(uuid);
            server.delete();
        } else {
            System.out.println("[Template.deleteServer] Server with UUID " + uuid.toString() + " is not part of Template " + this.name + "!");
        }
    }

    public void deleteServer(Server server) {
        //If Server is part of this template
        if (servers.containsValue(server)) {
            System.out.println("[Template.deleteServer] Deleting Server " + server.getName() + "!");

            //Remove Server from HashMap and delete it from Wrapper
            this.servers.remove(server.getUUID());
            server.delete();
        } else {
            System.out.println("[Template.deleteServer] Server " + server.getName() + " is not part of Template " + this.name + "!");
        }
    }


    public HashMap<UUID, Server> getServers() {
        return servers;
    }

    public void addServer(UUID uuid, Server server) {
        servers.put(uuid, server);
    }

    public String getName() {
        return name;
    }

    public int getRam() {
        return ram;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getEmptyservers() {
        return emptyservers;
    }

    public boolean isStartup() {
        return startup;
    }
}
