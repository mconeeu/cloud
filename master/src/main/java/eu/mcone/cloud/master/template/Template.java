/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.template;

import eu.mcone.cloud.core.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Log
public class Template {

    @Getter
    private Set<Server> servers;
    @Getter
    private String name, properties;
    @Getter
    private int maxPlayers, min, max, emptyservers;
    @Getter
    private long ram;
    @Getter
    private ServerVersion version;

    public Template(String name, long ram, int maxPlayers, int min, int max, int emptyservers, ServerVersion version, String properties) {
        this.servers = new HashSet<>();
        this.name = name;
        this.ram = ram;
        this.maxPlayers = maxPlayers;
        this.max = min;
        this.min = max;
        this.emptyservers = emptyservers;
        this.version = version;
        this.properties = properties;

        if (min > 0) {
            createServer(min);
        }
    }

    public void recreate(long ram, int maxPlayers, int min, int max, int emptyservers, ServerVersion version, String properties) {
        this.ram = ram;
        this.maxPlayers = maxPlayers;
        this.max = min;
        this.min = max;
        this.emptyservers = emptyservers;
        this.version = version;
        this.properties = properties;

        for (Server s : servers) {
            s.getInfo().setRam(ram);
            s.getInfo().setMaxPlayers(maxPlayers);
            s.getInfo().setVersion(version);
            s.getInfo().setProperties(properties);

            if (s.getWrapper() != null) s.getWrapper().send(new ServerInfoPacket(s.getInfo()));
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
                Server server = new Server(
                        new ServerInfo(
                                serverUUID,
                                servername,
                                name,
                                maxPlayers,
                                serverid,
                                ram,
                                false,
                                version,
                                properties
                        ),
                        this,
                        null
                );

                //Put Server Object in HashMap
                this.servers.add(server);
                log.info("["+name+"] Creating Server \"" + name + "-" + (actualservers + serverid) + "\"!");
            } else {
                log.info("["+name+"] Cannot create more Servers than maximum of group " + name + "!");
            }
        }
    }

    public void delete() {
        MasterServer.getInstance().unregisterTemplate(this);
        servers.forEach(Server::delete);
    }

    public void deleteServer(Server server) {
        //If Server is part of this template
        if (servers.contains(server)) {
            log.info("["+name+"] Deleting Server " + server.getInfo().getName() + "!");

            //Remove Server from HashMap and delete it from Wrapper
            this.servers.remove(server);
        } else {
            log.info("["+name+"] Server " + server.getInfo().getName() + " is not part of Template " + this.name + "!");
        }
    }

    @Override
    public String toString() {
        return name+" (Version: "+version+", RAM: "+ram+", Servers: "+servers.size()+")";
    }

}
