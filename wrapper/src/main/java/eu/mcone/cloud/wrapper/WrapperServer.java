/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.wrapper.network.ClientBootstrap;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.cloud.core.mysql.MySQL;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WrapperServer {

    private static WrapperServer instance;
    public static MySQL mysql_main;

    @Getter
    private String currentVersion = "1.0.0-SNAPSHOT";

    @Getter @Setter
    private Channel channel;
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

    public void shutdown() {
        System.out.println("[Shutdowm progress] Closing channel to Master...");
        channel.close();

        System.out.println("[Shutdowm progress] Stopping running servers...");
        for (Server s : servers) {
            s.sendcommand("stop");
        }

        try {
            System.out.println("[Shutdowm progress] Waiting for servers to stop...");
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Shutdowm progress] Stopping instance...");
        System.out.println("[Shutdowm progress] Good bye!");
        System.exit(0);
    }

    public static WrapperServer getInstance() {
        return instance;
    }

    public Server getServer(UUID uuid) {
        for (Server s : servers) {
            if (s.getInfo().getUuid().equals(uuid)) {
                return s;
            }
        }
        return null;
    }

}
