/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.core.console.ConsoleReader;
import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.file.CloudConfig;
import eu.mcone.cloud.core.file.Downloader;
import eu.mcone.cloud.core.file.FileManager;
import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.console.CommandExecutor;
import eu.mcone.cloud.wrapper.network.ClientBootstrap;
import eu.mcone.cloud.wrapper.server.Server;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WrapperServer {

    private static WrapperServer instance;

    @Getter
    private UUID wrapperUuid;
    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private FileManager fileManager;
    @Getter
    private CloudConfig config;
    @Getter
    private ClientBootstrap nettyBootstrap;
    @Getter @Setter
    private Channel channel;
    @Getter
    private long ram;
    @Getter
    private boolean shutdown = false;
    @Getter
    private LinkedHashSet<Server> servers = new LinkedHashSet<>();
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
        fileManager.createHomeDir("templates");
        fileManager.createHomeDir("servers");
        fileManager.createHomeDir("staticservers");
        fileManager.createHomeDir("config");
        fileManager.createHomeDir("jars");
        fileManager.createHomeDir("jars"+File.separator+"jenkins");

        consoleReader = new ConsoleReader();
        consoleReader.registerCommand(new CommandExecutor());

        threadPool = Executors.newCachedThreadPool();

        System.out.println("[Enable progress] Welcome to mc1cloud. Wrapper is starting...");

        config = new CloudConfig(new File(fileManager.getHomeDir()+File.separator+"config.yml"));
        try {
            wrapperUuid = UUID.fromString(config.getConfig().getString("uuid"));
            System.out.println("[Enable progress] Got wrapper UUID '"+wrapperUuid+"' from config...");
        } catch (IllegalArgumentException e) {
            UUID wrapperUuid = UUID.randomUUID();
            config.getConfig().set("uuid", wrapperUuid.toString());
            config.save();

            System.out.println("[Enable progress] Initialising new Wrapper with UUID '"+wrapperUuid+"'...");
            this.wrapperUuid = wrapperUuid;
        }

        System.out.println("[Enable progress] Downloading missing executeables for all ServerVersions:");
        try {
            for (ServerVersion v : ServerVersion.values()) {
                Downloader.download(v.getDownloadLink(), new File(fileManager.getHomeDir()+File.separator+"jars"+File.separator+v.toString()+".jar"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Enable progress] Trying to connect to master...");
        nettyBootstrap = new ClientBootstrap("localhost", 4567);
    }

    public void shutdown() {
        shutdown = true;

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
