/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master;

import eu.mcone.cloud.core.console.ConsoleReader;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.console.CommandExecutor;
import eu.mcone.cloud.master.network.ServerBootstrap;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.server.ServerManager;
import eu.mcone.cloud.master.server.StaticServerManager;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.core.mysql.Config;
import eu.mcone.cloud.core.mysql.MySQL;
import io.netty.channel.Channel;
import lombok.Getter;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MasterServer {

    @Getter
    private static MasterServer instance;
    private MySQL mysql;

    @Getter
    private Config config;
    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private ServerManager serverManager;
    @Getter
    private List<Template> templates = new ArrayList<>();
    @Getter
    private List<Wrapper> wrappers = new ArrayList<>();

    public static void main(String args[]) {
        new MasterServer();
    }

    private MasterServer() {
        instance = this;

        consoleReader = new ConsoleReader();
        consoleReader.registerCommand(new CommandExecutor());

        System.out.println("[Enable progress] Welcome to mc1cloud. Cloud is starting...");
        System.out.println("[Enable progress] Connecting to Database...");
        mysql = new MySQL("localhost", 3306, "cloud", "root", "", "cloudmaster");

        System.out.println("[Enable progress] Creating necessary tables if not exists...");
        mysql.createMasterTables();

        System.out.println("[Enable progress] Creating mysql config...");
        config = new Config(mysql, "mainconfig");
        config.createTable();

        System.out.println("[Enable progress] Insert ignore config values...");
        createConfigValues(config);

        System.out.println("[Enable progress] Getting templates from database...");
        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_templates;", rs -> {
            try {
                while (rs.next()) {
                    System.out.println("[Enable progress] Creating Template " + rs.getString("name") + " and it's servers ...");
                    new Template(
                            rs.getString("name"),
                            rs.getInt("ram"),
                            rs.getInt("max_players"),
                            rs.getInt("min"),
                            rs.getInt("max"),
                            rs.getInt("emptyservers"),
                            ServerVersion.valueOf(rs.getString("version")),
                            rs.getBoolean("startup")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        System.out.println("[Enable progress] Starting static server manager...");
        new StaticServerManager(mysql);

        System.out.println("[Enable progress] Starting ServerManager with TimeTask...");
        serverManager = new ServerManager();

        System.out.println("[Enable progress] Starting Netty Server...");
        new ServerBootstrap(4567);

        System.out.println("\nEnable process finished! Cloud Master seems to be ready! Waiting for connections...");
    }

    public void shutdown() {
        System.out.println("[Shutdowm progress] Stopping Wrapper Server...");
        serverManager.shutdown();

        for (Wrapper w : getWrappers()) {
            w.shutdown();
        }

        try {
            System.out.println("[Shutdowm progress] Waiting for wrappers to stop...");
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Shutdowm progress] Stopping instance...");
        System.out.println("[Shutdowm progress] Good bye!");
        System.exit(0);
    }

    private static void createConfigValues(final Config config) {
        Map<String, String> keys = new HashMap<>();

        //All Config keys and values
        keys.put("a", "1");
        keys.put("b", "2");

        //Insert into database
        keys.forEach(config::insert);
        System.out.println("[Enable progress] Config Values inserted!");
    }

    public Server getServer(UUID uuid) {
        for (Template t : templates) {
            for (Server s : t.getServers()) {
                if (s.getInfo().getUuid().equals(uuid)) {
                    return s;
                }
            }
        }
        return null;
    }

    public Server getServer(Channel channel) {
        for (Template t : templates) {
            for (Server s : t.getServers()) {
                if (s.getChannel().equals(channel)) {
                    return s;
                }
            }
        }
        return null;
    }

    public Collection<Server> getServers() {
        List<Server> result = new ArrayList<>();
        for (Template t : getTemplates()) result.addAll(t.getServers());
        return result;
    }

}
