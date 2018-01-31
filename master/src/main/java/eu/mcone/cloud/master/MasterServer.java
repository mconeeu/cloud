/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master;

import eu.mcone.cloud.master.network.ServerBootstrap;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.server.ServerManager;
import eu.mcone.cloud.master.server.StaticServerManager;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.core.mysql.Config;
import eu.mcone.cloud.core.mysql.MySQL;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MasterServer {

    @Getter
    private static MasterServer instance;
    public static String mysql_prefix = "cloudmaster";
    public static MySQL mysql_main;

    public static Config config;

    @Getter
    private List<Template> templates = new ArrayList<>();
    @Getter
    private List<Wrapper> wrappers = new ArrayList<>();

    public static void main(String args[]) {
        new MasterServer();
    }

    private MasterServer() {
        instance = this;

        System.out.println("[Enable progress] Welcome to mc1cloud. Cloud is starting...");
        System.out.println("[Enable progress] Connecting to Database...");
        mysql_main = new MySQL("localhost", 3306, "cloud", "root", "", "cloudmaster");
        mysql_main.connect();

        System.out.println("[Enable progress] Creating necessary tables if not exists...");
        mysql_main.createMasterTables();

        System.out.println("[Enable progress] Creating mysql config...");
        config = new Config(mysql_main, "mainconfig");
        config.createTable();

        System.out.println("[Enable progress] Insert ignore config values...");
        createConfigValues(config);

        System.out.println("[Enable progress] Getting templates from database...");
        ResultSet rs = mysql_main.select("SELECT * FROM " + mysql_prefix + "_templates;");
        try {
            while (rs.next()) {
                System.out.println("[Enable progress] Creating Template " + rs.getString("name") + " and it's servers ...");
                new Template(rs.getString("name"), rs.getInt("ram"), rs.getInt("max_players"), rs.getInt("min"), rs.getInt("max"), rs.getInt("emptyservers"), rs.getBoolean("startup"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("[Enable progress] Starting static server manager...");
        new StaticServerManager();

        System.out.println("[Enable progress] Starting ServerManager with TimeTask...");
        new ServerManager();

        System.out.println("[Enable progress] Starting Netty Server...");
        new ServerBootstrap(4567);

        System.out.println("\nEnable process finished! Cloud Master seems to be ready! Waiting for connections...");
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

}
