/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mcone.mc1cloud.wrapper;

import de.rufusmaiwald.mc1cloud.mysql.MySQL;
import de.rufusmaiwald.mcone.mc1cloud.wrapper.server.ServerManager;
import de.rufusmaiwald.mcone.mc1cloud.wrapper.server.Server;

import java.util.HashMap;
import java.util.UUID;

public class WrapperServer {

    public static MySQL mysql_main;

    public static HashMap<UUID, Server> servers = new HashMap<>();

    public static void main(String args[]) {
        System.out.println("[Enable progress] Welcome to mc1cloud. Wrapper is starting...");
        System.out.println("[Enable progress] Connecting to Database...");
        mysql_main = new MySQL("localhost", 3306, "cloud", "root", "", "cloudwrapper");
        mysql_main.connect();

        System.out.println("[Enable progress] Creating necessary tables if not exists...");
        mysql_main.createCloudTables();

        System.out.println("[Enable progress] Starting ServerManager with TimeTask...");
        new ServerManager();

        System.out.println("[Enable progress] Trying to connect to master...");
        /* ... */

    }

}
