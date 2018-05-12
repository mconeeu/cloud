/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper;

import eu.mcone.cloud.core.console.ConsoleColor;
import eu.mcone.cloud.core.console.ConsoleReader;
import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.file.CloudConfig;
import eu.mcone.cloud.core.file.Downloader;
import eu.mcone.cloud.core.file.FileManager;
import eu.mcone.cloud.core.mysql.MySQL;
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
    private MySQL mySQL;
    @Getter @Setter
    private Channel channel;
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

        this.ram = Runtime.getRuntime().maxMemory();
        this.ram /= (1024 * 1024);
        Logger.log(getClass(), ram+"M RAM");

        fileManager = new FileManager();
        fileManager.createHomeDir("templates");
        fileManager.createHomeDir("servers");
        fileManager.createHomeDir("staticservers");
        fileManager.createHomeDir("jars");
        fileManager.createHomeDir("jars"+File.separator+"jenkins");
        fileManager.createHomeDir("worlds");

        consoleReader = new ConsoleReader();
        consoleReader.registerCommand(new CommandExecutor());

        threadPool = Executors.newCachedThreadPool();

        Logger.log("Enable progress", ConsoleColor.CYAN+"Welcome to mc1cloud. Wrapper is starting...");

        Logger.log("Enable progress", "Connecting to Database...");
        mySQL = new MySQL("mysql.mcone.eu", 3306, "mc1cloud", "cloud-system", "5CjLP5dHYXQPX85zPizx5hayz0AYNOuNmzcegO0Id0AXnp3w1OJ3fkEQxbGJZAuJ", "cloudwrapper");
        createMySQLTables(mySQL);

        config = new CloudConfig(new File(fileManager.getHomeDir()+File.separator+"config.yml"), "jenkins", "worlds");
        try {
            wrapperUuid = UUID.fromString(config.getConfig().getString("uuid"));
            Logger.log("Enable progress", "Got wrapper UUID '"+wrapperUuid+"' and Master IP from config...");
        } catch (IllegalArgumentException e) {
            UUID wrapperUuid = UUID.randomUUID();
            config.getConfig().set("uuid", wrapperUuid.toString());
            config.getConfig().set("master-hostname", "localhost");
            config.getConfig().set("master-port", 4567);
            config.save();

            Logger.log("Enable progress", "Initialising new Wrapper with UUID '"+wrapperUuid+"'...");
            this.wrapperUuid = wrapperUuid;
        }

        Logger.log("Enable progress", "Downloading missing executeables for all ServerVersions:");
        try {
            for (ServerVersion v : ServerVersion.values()) {
                Downloader.download(v.getDownloadLink(), new File(fileManager.getHomeDir()+File.separator+"jars"+File.separator+v.toString()+".jar"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.log("Enable progress", "Trying to connect to master...");
        nettyBootstrap = new ClientBootstrap(config.getConfig().getString("master-hostname"), config.getConfig().getInt("master-port"));

        Logger.log("Enable progress", ConsoleColor.GREEN+"Enable process finished! CloudWrapper seems to be ready! Waiting for connections...\n");
    }

    private void createMySQLTables(MySQL mySQL) {
        mySQL.update(
                "CREATE TABLE IF NOT EXISTS `"+mySQL.getTablePrefix()+"_worlds`" +
                    "(" +
                    "`id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "`build` int(10) NOT NULL," +
                    "`name` VARCHAR(100) NOT NULL," +
                    "`world_type` VARCHAR(20) NOT NULL," +
                    "`environment` VARCHAR(20) NOT NULL," +
                    "`difficulty` VARCHAR(20) NOT NULL," +
                    "`spawn_location` VARCHAR(100) NOT NULL," +
                    "`generator` VARCHAR(50)," +
                    "`properties` VARCHAR(1000) NOT NULL," +
                    "`bytes` longblob NOT NULL " +
                    ")" +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8;"
        );
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
