/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.core.network.ChannelPacketHandler;
import eu.mcone.cloud.core.network.ClientBootstrap;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.cloud.wrapper.server.ServerManager;
import eu.mcone.cloud.core.mysql.MySQL;

import java.util.HashMap;
import java.util.UUID;

public class WrapperServer {

    public static MySQL mysql_main;
    public static ChannelPacketHandler cph;

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
        ClientBootstrap cb = new ClientBootstrap("localhost", 4567);
        cph = cb.getCph();
    }

}
