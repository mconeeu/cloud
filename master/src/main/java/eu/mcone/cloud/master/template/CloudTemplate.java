/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.template;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.api.template.Template;
import eu.mcone.cloud.core.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerProperties;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.CloudServer;
import eu.mcone.networkmanager.host.api.ModuleHost;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Log
public class CloudTemplate implements Template {

    @Getter
    private Set<Server> servers;
    @Getter
    private String name, properties;
    @Getter
    private int maxPlayers, minServers, maxServers, emptyServers;
    @Getter
    private long ram;
    @Getter
    private ServerVersion version;

    public CloudTemplate(String name, long ram, int maxPlayers, int minServers, int maxServers, int emptyServers, ServerVersion version, String properties) {
        this.servers = new HashSet<>();
        this.name = name;
        this.ram = ram;
        this.maxPlayers = maxPlayers;
        this.minServers = minServers;
        this.maxServers = maxServers;
        this.emptyServers = emptyServers;
        this.version = version;
        this.properties = properties;

        if (minServers > 0) {
            createServer(minServers);
        }
    }

    public void recreate(long ram, int maxPlayers, int min, int max, int emptyServers, ServerVersion version, String properties) {
        this.ram = ram;
        this.maxPlayers = maxPlayers;
        this.maxServers = min;
        this.minServers = max;
        this.emptyServers = emptyServers;
        this.version = version;
        this.properties = properties;

        for (Server s : servers) {
            s.getInfo().setRam(ram);
            s.getInfo().setMaxPlayers(maxPlayers);
            s.getInfo().setVersion(version);
            s.getInfo().setProperties(properties);

            if (s.getWrapper() != null) ((CloudServer) s).getWrapper().send(new ServerInfoPacket(s.getInfo()));
        }

        if (servers.size() < min) {
            createServer(min-servers.size());
        }
    }

    public void createServer(int amount) {
        //For the amount of required servers
        for (int i=1; i<=amount; i++) {
            int actualservers = this.servers.size();
            int serverid= actualservers+i;

            if(serverid <= this.maxServers) {
                //Template name with "-" and a new unused number
                String servername = this.name+"-"+Integer.toString(serverid);
                UUID serverUUID = UUID.randomUUID();

                //Create server object
                CloudServer server = new CloudServer(
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
        MasterServer.getServer().unregisterTemplate(this);
        servers.forEach(Server::delete);
    }

    public void deleteServer(CloudServer server) {
        //If Server is part of this template
        if (servers.contains(server)) {
            log.info("["+name+"] Deleting Server " + server.getInfo().getName() + "!");

            //Remove Server from HashMap and delete it from Wrapper
            this.servers.remove(server);
        } else {
            log.info("["+name+"] Server " + server.getInfo().getName() + " is not part of Template " + this.name + "!");
        }
    }

    public ServerProperties getProperties() {
        return ModuleHost.getInstance().getGson().fromJson(properties, ServerProperties.class);
    }

    @Override
    public String toString() {
        return name+" (Version: "+version+", RAM: "+ram+", Servers: "+servers.size()+")";
    }

}
