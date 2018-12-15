/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.server;

import eu.mcone.cloud.core.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.networkmanager.api.ModuleHost;
import eu.mcone.networkmanager.core.api.database.Database;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bson.Document;

import java.util.*;

@Log
public class StaticServerManager {

    @Getter
    private List<Server> servers;

    public StaticServerManager() {
        this.servers = new ArrayList<>();
    }

    public void initialize() {
        serverLoop:
        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_static_servers").find()) {
            for (Template t : MasterServer.getInstance().getTemplates()) {
                if (entry.getString("name").startsWith(t.getName())) {
                    log.severe("Could not create static server "+entry.getString("name")+": name starts with name of an existing Template"+t.getName());
                    continue serverLoop;
                }
            }

            log.info("Creating static Server " + entry.getString("name") + "...");
            initializeServer(
                    entry.getString("name"),
                    entry.getInteger("max"),
                    entry.getLong("ram"),
                    ServerVersion.valueOf(entry.getString("version")),
                    UUID.fromString(entry.getString("wrapper"))
            );
        }
    }

    public void reload() {
        final Map<String, Server> oldServers = new HashMap<>();
        servers.forEach(s -> oldServers.put(s.getInfo().getName(), s));

        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_static_servers").find()) {
            if (oldServers.containsKey(entry.getString("name"))) {
                log.info("Reload progress - Recreating static Server " + entry.getString("name") + "...");
                Server s = oldServers.get(entry.getString("name"));

                if (s.getInfo().isStaticServer() && (s.getWrapperUuid().equals(UUID.fromString(entry.getString("wrapper"))) || s.getWrapper() == null)) {
                    s.setWrapperUuid(UUID.fromString(entry.getString("wrapper")));
                    s.getInfo().setMaxPlayers(entry.getInteger("max"));
                    s.getInfo().setRam(entry.getLong("ram"));
                    s.getInfo().setVersion(ServerVersion.valueOf(entry.getString("version")));

                    if (s.getWrapper() != null) s.getWrapper().send(new ServerInfoPacket(s.getInfo()));
                } else {
                    s.delete();

                    initializeServer(
                            entry.getString("name"),
                            entry.getInteger("max"),
                            entry.getLong("ram"),
                            ServerVersion.valueOf(entry.getString("version")),
                            UUID.fromString(entry.getString("wrapper"))
                    );
                }
            } else {
                log.info("Reload progress - Adding static Server " + entry.getString("name") + "...");

                initializeServer(
                        entry.getString("name"),
                        entry.getInteger("max"),
                        entry.getLong("ram"),
                        ServerVersion.valueOf(entry.getString("version")),
                        UUID.fromString(entry.getString("wrapper"))
                );
            }

            oldServers.remove(entry.getString("name"));
        }

        for (Server s : oldServers.values()) {
            log.info("Reload progress - Deleting old static Server " + s.getInfo().getName() + "...");
            servers.remove(s);
            s.delete();
        }
    }

    private void initializeServer(String name, int max, long ram, ServerVersion version, UUID wrapperUuid) {
        UUID uuid = UUID.randomUUID();
        servers.add(
                new Server(
                        new ServerInfo(
                                uuid,
                                name,
                                "",
                                max,
                                0,
                                ram,
                                true,
                                version,
                                "{\"plugins\":[], \"worlds\":[], \"gamemodeType\":[], \"configs\":[]}"
                        ),
                        null,
                        wrapperUuid
                )
        );
    }

    public void addStaticServer(String name, int max, long ram, ServerVersion version, UUID wrapperUuid) {
        log.info("Creating new static Server " + name + "...");

        ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_static_servers").insertOne(
                new Document("name", name)
                        .append("max", max)
                        .append("ram", ram)
                        .append("version", version.toString())
                        .append("wrapper", wrapperUuid.toString())
        );

        initializeServer(name, max, ram, version, wrapperUuid);
    }

    public void deleteServer(Server s) {
        servers.remove(s);
    }

}
