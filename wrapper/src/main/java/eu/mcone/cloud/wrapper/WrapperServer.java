/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.wrapper.network.ClientBootstrap;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.cloud.core.mysql.MySQL;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WrapperServer {

    private static WrapperServer instance;
    public static MySQL mysql_main;

    @Getter
    private String currentVersion = "1.0.0-SNAPSHOT";

    @Getter @Setter
    private Channel channel;
    @Getter
    private Server sr;
    @Getter
    private int ram;
    @Getter
    private List<Server> servers = new ArrayList<>();

    public static void main(String args[]) {
        new WrapperServer(2048);
    }

    private WrapperServer(int ram) {
        instance = this;
        this.ram = ram;

        this.sr = new Server(new ServerInfo(UUID.fromString("5139fcd7-7c3f-4cd4-8d76-5f365c36d9e5"), "test", "Test", 10, 10, 1));
        servers.add(sr);

        //Server s = new Server(new ServerInfo(UUID.randomUUID(),"Skypvp", "Test", 5, 10, 1));
        //s.start();

        System.out.println("[Enable progress] Welcome to mc1cloud. Wrapper is starting...");
        System.out.println("[Enable progress] Connecting to Database...");
        mysql_main = new MySQL("localhost", 3306, "cloud", "root", "", "cloudwrapper");
        mysql_main.connect();

        System.out.println("[Enable progress] Creating necessary tables if not exists...");

        System.out.println("[Enable progress] Trying to connect to master...");
        new ClientBootstrap("localhost", 4567);
    }

    public static WrapperServer getInstance() {
        return instance;
    }

}