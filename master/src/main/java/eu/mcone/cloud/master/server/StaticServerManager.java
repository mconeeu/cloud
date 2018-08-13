/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.networkmanager.api.ModuleHost;
import eu.mcone.networkmanager.core.api.database.Database;
import lombok.Getter;
import org.bson.Document;

import java.util.*;

public class StaticServerManager {

    @Getter
    private List<Server> servers = new ArrayList<>();

    public StaticServerManager() {
        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_static_servers").find()) {
            Logger.log(getClass(), "Creating Static Server " + entry.getString("name") + "...");

            //Create Server and store in HashMap
            UUID uuid = UUID.randomUUID();
            servers.add(
                    new Server(
                            new ServerInfo(
                                    uuid,
                                    entry.getString("name"),
                                    "",
                                    entry.getInteger("max"),
                                    0,
                                    entry.getInteger("ram"),
                                    true,
                                    ServerVersion.valueOf(entry.getString("version")),
                                    "{\"plugins\":[], \"worlds\":[], \"gamemodeType\":[], \"configs\":[]}"
                            ),
                            null,
                            UUID.fromString(entry.getString("wrapper"))
                    )
            );
        }
    }

    public void reload() {
        final Map<String, Server> oldServers = new HashMap<>();
        servers.forEach(s -> oldServers.put(s.getInfo().getName(), s));

        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_static_servers").find()) {
            if (oldServers.containsKey(entry.getString("name"))) {
                Logger.log("Reload progress", "Recreating static Server " + entry.getString("name") + "...");

                Server s = oldServers.get(entry.getString("name"));
                s.getInfo().setMaxPlayers(entry.getInteger("max"));
                s.getInfo().setRam(entry.getLong("ram"));
                s.getInfo().setVersion(ServerVersion.valueOf(entry.getString("version")));

                if (s.getWrapper() != null) s.getWrapper().send(new ServerInfoPacket(s.getInfo()));
            } else {
                Logger.log("Reload progress", "Adding static Server " + entry.getString("name") + "...");

                UUID uuid = UUID.randomUUID();
                servers.add(
                        new Server(
                                new ServerInfo(
                                        uuid,
                                        entry.getString("name"),
                                        "",
                                        entry.getInteger("max"),
                                        0,
                                        entry.getLong("ram"),
                                        true,
                                        ServerVersion.valueOf(entry.getString("version")),
                                        "{\"plugins\":[], \"worlds\":[], \"gamemode\":[], \"mode\":[], \"configs\":[]}"
                                ),
                                null,
                                UUID.fromString(entry.getString("wrapper"))
                        )
                );
            }

            oldServers.remove(entry.getString("name"));
        }

        for (Server s : oldServers.values()) {
            Logger.log("Reload progress", "Deleting old static Server " + s.getInfo().getName() + "...");
            servers.remove(s);
            s.delete();
        }
    }

    public void addStaticServer(String name, int maxPlayers, long ram, ServerVersion version, String wrappername) {
        Logger.log(getClass(), "Creating Static Server " + name + "...");

        ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_static_servers").insertOne(
                new Document("name", name)
                        .append("max", maxPlayers)
                        .append("ram", ram)
                        .append("version", version.toString())
                        .append("wrapper", wrappername)
        );

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
