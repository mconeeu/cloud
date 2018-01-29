/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.wrapper.console.Reader;
import eu.mcone.cloud.wrapper.network.ChannelPacketHandler;
import eu.mcone.cloud.wrapper.network.ClientBootstrap;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.cloud.core.mysql.MySQL;
import eu.mcone.cloud.wrapper.server.ServerManager;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WrapperServer {

    public static MySQL mysql_main;
    //public static ChannelPacketHandler cph;

    public static final String current_version = "1.0-Snapshot";
    public static HashMap<UUID, Server> servers = new HashMap<>();
    public static List<ChannelHandlerContext> connections = new ArrayList<>();

    public static void main(String args[]) {

        Reader rd = new Reader();

        try {
            //Server sr = new Server(new ServerInfo(UUID.randomUUID(),"Skypvp", "Test", 5, 10, 1));
            //sr.start();
        } catch (Exception e) {
            System.out.println("[WRAPPER] Es ist ein Fehler in der Klasse WrapperServer aufgetreten");
            e.printStackTrace();
        }


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
        //cph = cb.getCph();
    }

}
