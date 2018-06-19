/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.mysql.MySQL;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import lombok.Getter;

import java.sql.SQLException;
import java.util.*;

public class StaticServerManager {

    @Getter
    private List<Server> servers = new ArrayList<>();
    private MySQL mysql;

    public StaticServerManager(MySQL mysql) {
        this.mysql = mysql;

        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_static_servers;", rs -> {
            try {
                while (rs.next()) {
                    Logger.log(getClass(), "Creating Static Server " + rs.getString("name") + "...");

                    //Create Server and store in HashMap
                    UUID uuid = UUID.randomUUID();
                    servers.add(
                            new Server(
                                    new ServerInfo(
                                            uuid,
                                            rs.getString("name"),
                                            "",
                                            rs.getInt("max"),
                                            0,
                                            rs.getInt("ram"),
                                            true,
                                            ServerVersion.valueOf(rs.getString("version")),
                                            "{\"plugins\":[], \"worlds\":[], \"gamemodeType\":[], \"configs\":[]}"
                                    ),
                                    null,
                                    UUID.fromString(rs.getString("wrapper"))
                            )
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void reload() {
        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_static_servers;", rs -> {
            final Map<String, Server> oldServers = new HashMap<>();
            final List<String> newServers = new ArrayList<>();
            servers.forEach(s -> oldServers.put(s.getInfo().getName(), s));

            try {
                while (rs.next()) {
                    if (oldServers.containsKey(rs.getString("name"))) {
                        Logger.log("Reload progress", "Recreating static Server " + rs.getString("name") + "...");

                        Server s = oldServers.get(rs.getString("name"));
                        s.getInfo().setMaxPlayers(rs.getInt("max"));
                        s.getInfo().setRam(rs.getLong("ram"));
                        s.getInfo().setVersion(ServerVersion.valueOf(rs.getString("version")));

                        if (s.getWrapper() != null) s.getWrapper().send(new ServerInfoPacket(s.getInfo()));
                    } else {
                        Logger.log("Reload progress", "Adding static Server " + rs.getString("name") + "...");

                        UUID uuid = UUID.randomUUID();
                        servers.add(
                                new Server(
                                        new ServerInfo(
                                                uuid,
                                                rs.getString("name"),
                                                "",
                                                rs.getInt("max"),
                                                0,
                                                rs.getLong("ram"),
                                                true,
                                                ServerVersion.valueOf(rs.getString("version")),
                                                "{\"plugins\":[], \"worlds\":[], \"gamemode\":[], \"mode\":[], \"configs\":[]}"
                                        ),
                                        null,
                                        UUID.fromString(rs.getString("wrapper"))
                                )
                        );
                    }

                    newServers.add(rs.getString("name"));
                    oldServers.remove(rs.getString("name"));
                }

                for (Server s : oldServers.values()) {
                    Logger.log("Reload progress", "Deleting old static Server " + s.getInfo().getName() + "...");
                    servers.remove(s);
                    s.delete();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addStaticServer(String name, int maxPlayers, long ram, ServerVersion version, String wrappername) {
        Logger.log(getClass(), "Creating Static Server " + name + "...");
        mysql.update("INSERT INTO " + mysql.getTablePrefix() + "_static_servers (name, ram, wrapper) VALUES ('" + name + "'," + ram + " , '" + wrappername + "');");

        UUID uuid = UUID.randomUUID();
        servers.add(
                new Server(
                        new ServerInfo(
                                uuid,
                                name,
                                "",
                                maxPlayers,
                                0,
                                ram,
                                true,
                                version,
                                "{\"plugins\":[], \"worlds\":[], \"gamemode\":[], \"mode\":[], \"configs\":[]}"
                        ),
                        null,
                        UUID.fromString(wrappername)
                )
        );
    }

}
