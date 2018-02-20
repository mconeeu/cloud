/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.file.FileManager;
import eu.mcone.cloud.core.console.ConsoleReader;
import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.console.CommandExecutor;
import eu.mcone.cloud.wrapper.network.ClientBootstrap;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.cloud.core.mysql.MySQL;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WrapperServer {

    private static WrapperServer instance;
    private final MySQL mysql;

    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private FileManager fileManager;
    @Getter
    private ClientBootstrap nettyBootstrap;
    @Getter @Setter
    private Channel channel;
    @Getter
    private long ram;
    @Getter
    private List<Server> servers = new ArrayList<>();
    @Getter
    private ExecutorService threadPool;

    public static void main(String args[]) {
        new WrapperServer();
    }

    private WrapperServer() {
        instance = this;

        this.ram = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        Logger.log(getClass(), ram+"M RAM");

        fileManager = new FileManager();
        fileManager.createHomeDir("wrapper");
        fileManager.createHomeDir("wrapper"+File.separator+"templates");
        fileManager.createHomeDir("wrapper"+File.separator+"servers");
        fileManager.createHomeDir("wrapper"+File.separator+"config");

        consoleReader = new ConsoleReader();
        consoleReader.registerCommand(new CommandExecutor());

        threadPool = Executors.newCachedThreadPool();

        System.out.println("[Enable progress] Welcome to mc1cloud. Wrapper is starting...");
        System.out.println("[Enable progress] Connecting to Database...");
        mysql = new MySQL("localhost", 3306, "cloud", "root", "", "cloudwrapper");

        System.out.println("[Enable progress] Creating necessary tables if not exists...");

        System.out.println("[Enable progress] Trying to connect to master...");
        nettyBootstrap = new ClientBootstrap("localhost", 4567);
    }

    public void shutdown() {
        System.out.println("[Shutdowm progress] Closing channel to Master...");
        channel.close();

        System.out.println("[Shutdowm progress] Stopping running servers...");
        for (Server s : servers) {
            s.stop();
        }

        try {
            System.out.println("[Shutdowm progress] Waiting for servers to stop...");
            TimeUnit.SECONDS.sleep(10);
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

    public Server getSpigotServer(UUID uuid) {
        for (Server s : servers) {
            if (s.getInfo().getVersion().equals(ServerVersion.SPIGOT) && s.getInfo().getUuid().equals(uuid)) {
                return s;
            }
        }
        return null;
    }

    public Server getBungeeServer(UUID uuid) {
        for (Server s : servers) {
            if (s.getInfo().getVersion().equals(ServerVersion.BUNGEE) && s.getInfo().getUuid().equals(uuid)) {
                return s;
            }
        }
        return null;
    }

    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public void send(Packet packet) {
        if (channel.isOpen()) {
            channel.writeAndFlush(packet);
        } else {
            Logger.log(getClass(), "Cannot send packet "+packet.getClass().getSimpleName()+" to Master because channel is closed!");
        }
    }

}
