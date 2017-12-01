/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mcone.mc1cloud.master.server;

import de.rufusmaiwald.mcone.mc1cloud.master.MasterServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class StaticServerManager {

    private static HashMap<UUID, Server> staticServers = new HashMap<>();

    public StaticServerManager() {
        ResultSet rs = MasterServer.mysql_main.select("SELECT * FROM " + MasterServer.mysql_prefix + "_static_servers;");
        try {
            while (rs.next()) {
                System.out.println("[StaticServerManager.class] Creating Static Server " + rs.getString("name") + "...");

                //Create Server and store in HashMap
                UUID uuid = UUID.randomUUID();
                staticServers.put(uuid, new Server(uuid, rs.getString("name"), null, 0, rs.getInt("ram"), rs.getString("wrapper")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addStaticServer(String name, int ram, String wrappername) {
        System.out.println("[StaticServerManager.class] Creating Static Server " + name + "...");
        MasterServer.mysql_main.execute("INSERT INTO " + MasterServer.mysql_prefix + "_static_servers (name, ram, wrapper) VALUES ('" + name + "'," + Integer.valueOf(ram).toString() + " , '" + wrappername + "');");

        UUID uuid = UUID.randomUUID();
        staticServers.put(uuid, new Server(uuid, name, null, 0, ram, wrappername));
    }

    public static void deleteStaticServer(Server server) {
        if (staticServers.containsValue(server)) {
            System.out.println("[StaticServerManager.class] Removing Static Server " + server.getName() + "...");
            MasterServer.mysql_main.execute("DELETE FROM " + MasterServer.mysql_prefix + "_static_servers WHERE name='" + server.getName() + "';");

            server.delete();
            staticServers.remove(server.getUUID());
        }
    }

    public HashMap<UUID, Server> getStaticServers() {
        return staticServers;
    }
}
