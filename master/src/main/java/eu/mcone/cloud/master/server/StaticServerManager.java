/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.mysql.MySQL;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StaticServerManager {

    private List<Server> staticServers = new ArrayList<>();
    private MySQL mysql;

    public StaticServerManager(MySQL mysql) {
        this.mysql = mysql;

        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_static_servers;", rs -> {
            try {
                while (rs.next()) {
                    Logger.log(getClass(), "Creating Static Server " + rs.getString("name") + "...");

                    //Create Server and store in HashMap
                    UUID uuid = UUID.randomUUID();
                    staticServers.add(
                            new Server(
                                    new ServerInfo(
                                            uuid,
                                            rs.getString("name"),
                                            null,
                                            rs.getInt("max"),
                                            0,
                                            rs.getInt("ram"),
                                            ServerVersion.valueOf(rs.getString("version"))
                                    ),
                                    null,
                                    rs.getString("wrapper")
                            )
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addStaticServer(String name, int maxPlayers, int ram, ServerVersion version, String wrappername) {
        Logger.log(getClass(), "Creating Static Server " + name + "...");
        mysql.update("INSERT INTO " + mysql.getTablePrefix() + "_static_servers (name, ram, wrapper) VALUES ('" + name + "'," + Integer.valueOf(ram).toString() + " , '" + wrappername + "');");

        UUID uuid = UUID.randomUUID();
        staticServers.add(
                new Server(
                        new ServerInfo(
                                uuid,
                                name,
                                null,
                                maxPlayers,
                                0,
                                ram,
                                version
                        ),
                        null,
                        wrappername
                )
        );
    }

    public void deleteStaticServer(Server server) {
        if (staticServers.contains(server)) {
            Logger.log(getClass(), "Removing Static Server " + server.getInfo().getName() + "...");
            mysql.update("DELETE FROM " + mysql.getTablePrefix() + "_static_servers WHERE name='" + server.getInfo().getName() + "';");

            server.delete();
            staticServers.remove(server);
        }
    }

    public List<Server> getStaticServers() {
        return staticServers;
    }
}
