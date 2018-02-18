/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.template;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import lombok.Getter;

import java.util.*;

public class Template {

    @Getter
    private List<Server> servers = new ArrayList<>();

    @Getter
    private String name;
    @Getter
    private int ram, maxPlayers, min, max, emptyservers;
    @Getter
    private boolean startup;
    @Getter
    private ServerVersion version;

    public Template(String name, int ram, int maxPlayers, int min, int max, int emptyservers, ServerVersion version, boolean startup) {
        this.name = name;
        this.ram = ram;
        this.maxPlayers = maxPlayers;
        this.max = min;
        this.min = max;
        this.emptyservers = emptyservers;
        this.version = version;
        this.startup = startup;

        MasterServer.getInstance().getTemplates().add(this);

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
                Server server = new Server(
                        new ServerInfo(
                                serverUUID,
                                servername,
                                name,
                                serverid,
                                maxPlayers,
                                ram,
                                version
                        ),
                        this,
                        null
                );

                //Put Server Object in HashMap
                this.servers.add(server);
                Logger.log(getClass(), "["+name+"] Creating Server \"" + name + "-" + (actualservers + serverid) + "\"!");
            } else {
                Logger.err(getClass(), "["+name+"] Cannot create more Servers than maximum of group " + name + "!");
            }
        }
    }

    public void deleteServer(UUID uuid) {
        //If Server is part of this template
        for (Server s : servers) {
            if (s.getInfo().getUuid().equals(uuid)) {
                Logger.log(getClass(), "["+name+"] Deleting Server " + s.getInfo().getName() + "!");

                //Remove Server from HashMap and delete it from Wrapper
                this.servers.remove(s);
                s.delete();
                return;
            }
        }

        Logger.err(getClass(), "["+name+"] Server with UUID " + uuid.toString() + " is not part of Template " + this.name + "!");
    }

    public void deleteServer(Server server) {
        //If Server is part of this template
        if (servers.contains(server)) {
            Logger.log(getClass(), "["+name+"] Deleting Server " + server.getInfo().getName() + "!");

            //Remove Server from HashMap and delete it from Wrapper
            this.servers.remove(server);
            server.delete();
        } else {
            Logger.err(getClass(), "["+name+"] Server " + server.getInfo().getName() + " is not part of Template " + this.name + "!");
        }
    }

    public void addServer(Server server) {
        servers.add(server);
    }

}
