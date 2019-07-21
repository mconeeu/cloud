/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import com.google.gson.Gson;
import com.mongodb.client.MongoDatabase;
import eu.mcone.cloud.core.file.CloudConfig;
import eu.mcone.cloud.core.file.Downloader;
import eu.mcone.cloud.core.file.FileManager;
import eu.mcone.cloud.core.packet.*;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.console.ConsoleCommandExecutor;
import eu.mcone.cloud.wrapper.download.GitlabArtifactDownloader;
import eu.mcone.cloud.wrapper.handler.*;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.networkmanager.api.packet.Packet;
import eu.mcone.networkmanager.client.ClientBootstrap;
import eu.mcone.networkmanager.client.NetworkmanagerClient;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import eu.mcone.networkmanager.core.api.database.Database;
import eu.mcone.networkmanager.core.console.ConsoleReader;
import eu.mcone.networkmanager.core.console.log.MconeLogger;
import eu.mcone.networkmanager.core.database.MongoConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log
public class WrapperServer {

    @Getter
    private static WrapperServer instance;

    @Getter
    private MconeLogger mconeLogger;
    @Getter
    private UUID wrapperUuid;
    @Getter
    private long ram;
    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private FileManager fileManager;
    @Getter
    private CloudConfig config;
    @Getter
    private ClientBootstrap nettyBootstrap;
    @Getter
    private MongoConnection mongoConnection;
    @Getter
    private MongoDatabase mongoDB;
    @Getter
    private Gson gson;
    @Getter
    private GitlabArtifactDownloader gitlabArtifactDownloader;
    @Getter
    @Setter
    private Channel channel;
    @Getter
    private boolean shutdown = false;
    @Getter
    private LinkedHashSet<Server> servers = new LinkedHashSet<>();
    @Getter
    private ExecutorService threadPool;

    public static void main(String[] args) {
        new WrapperServer();
    }

    private WrapperServer() {
        instance = this;
        mconeLogger = new MconeLogger();

        this.ram = Runtime.getRuntime().maxMemory();
        this.ram /= (1024 * 1024);
        log.info(ram + "M RAM");

        fileManager = new FileManager();
        fileManager.createHomeDir("servers");
        fileManager.createHomeDir("staticservers");
        fileManager.createHomeDir("jars");
        fileManager.createHomeDir("jars" + File.separator + "gitlab");
        fileManager.createHomeDir("worlds");

        consoleReader = new ConsoleReader();
        consoleReader.registerCommand(new ConsoleCommandExecutor());

        gson = new Gson();

        threadPool = Executors.newCachedThreadPool();

        log.info("Enable progress - " + ConsoleColor.AQUA + "Welcome to mc1cloud. Wrapper is starting...");

        log.info("Enable progress - Connecting to Database...");
        mongoConnection = new MongoConnection("db.mcone.eu", "admin", "T6KIq8gjmmF1k7futx0cJiJinQXgfguYXruds1dFx1LF5IsVPQjuDTnlI1zltpD9", "admin", 27017);
        mongoConnection.connect();

        mongoDB = mongoConnection.getDatabase(Database.CLOUD);

        config = new CloudConfig(new File(fileManager.getHomeDir() + File.separator + "config.yml"), "jenkins", "worlds");
        try {
            wrapperUuid = UUID.fromString(config.getConfig().getString("uuid"));
            log.info("Enable progress - Got wrapper UUID '" + wrapperUuid + "' and Master IP from config...");
        } catch (IllegalArgumentException e) {
            UUID wrapperUuid = UUID.randomUUID();
            config.getConfig().set("uuid", wrapperUuid.toString());
            config.getConfig().set("master-hostname", "localhost");
            config.save();

            log.info("Enable progress - Initialising new Wrapper with UUID '" + wrapperUuid + "'...");
            this.wrapperUuid = wrapperUuid;
        }

        gitlabArtifactDownloader = new GitlabArtifactDownloader();

        log.info("Enable progress - Downloading missing executeables for all ServerVersions:");
        for (ServerVersion v : ServerVersion.values()) {
            try {
                Downloader.download(v.getDownloadLink(), new File(fileManager.getHomeDir() + File.separator + "jars" + File.separator + v.toString() + ".jar"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.info("Enable progress - Trying to connect to master...");
        nettyBootstrap = new ClientBootstrap(config.getConfig().getString("master-hostname"), "eu.mcone.cloud.wrapper", new NetworkmanagerClient() {
            @Override
            public void runAsync(Runnable runnable) {
                threadPool.execute(runnable);
            }
            @Override
            public void onChannelActive(ChannelHandlerContext chc) {
                channel = chc.channel();
                registerPacketHandlers();

                if (WrapperServer.getInstance().getServers().size() < 1) {
                    chc.writeAndFlush(new WrapperRegisterPacketWrapper(WrapperServer.getInstance().getRam(), WrapperServer.getInstance().getWrapperUuid()));
                } else {
                    Map<UUID, String> serverMap = new HashMap<>();
                    servers.forEach(server -> serverMap.put(server.getInfo().getUuid(), server.getInfo().getName()));

                    chc.writeAndFlush(new WrapperRegisterFromStandalonePacketWrapper(WrapperServer.getInstance().getRam(), WrapperServer.getInstance().getWrapperUuid(), serverMap));
                }
            }
            @Override
            public void onChannelUnregistered(ChannelHandlerContext chc) {}
        });

        log.info("Enable progress - "+ConsoleColor.GREEN + "Enable process finished! CloudWrapper seems to be ready! Waiting for connections...\n");
    }

    public void shutdown() {
        shutdown = true;

        log.info("Shutdowm progress - Closing channel to Master...");
        channel.close();

        log.info("Shutdowm progress - Stopping running servers...");
        for (Server s : servers) {
            s.doStop();
        }

        try {
            log.info("Shutdowm progress - Waiting for servers to stop...");
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Shutdowm progress - Stopping instance...");
        log.info("Shutdowm progress - Good bye!");
        System.exit(0);
    }

    private void registerPacketHandlers() {
        nettyBootstrap.getPacketManager().registerPacketHandler(ServerChangeStatePacketWrapper.class, new ServerChangeStateHandler());
        nettyBootstrap.getPacketManager().registerPacketHandler(ServerCommandExecutePacketWrapper.class, new ServerCommandExecuteHandler());
        nettyBootstrap.getPacketManager().registerPacketHandler(ServerInfoPacket.class, new ServerInfoHandler());
        nettyBootstrap.getPacketManager().registerPacketHandler(WrapperShutdownPacketWrapper.class, new WrapperShutdownHandler());
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
            log.info("Cannot send packet " + packet.getClass().getSimpleName() + " to Master because channel is closed!");
        }
    }

}
