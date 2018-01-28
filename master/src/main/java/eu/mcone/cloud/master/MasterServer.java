/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master;

import eu.mcone.cloud.master.console.Reader;
import eu.mcone.cloud.master.network.ChannelPacketHandler;
import eu.mcone.cloud.master.network.ServerBootstrap;
import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacket;
import eu.mcone.cloud.master.server.ServerManager;
import eu.mcone.cloud.master.server.StaticServerManager;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.cloud.core.mysql.Config;
import eu.mcone.cloud.core.mysql.MySQL;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MasterServer {

    public static String mysql_prefix = "cloudmaster";
    public static MySQL mysql_main;

    public static Config config;

    public static List<ChannelHandlerContext> connections = new ArrayList<>();
    public static HashMap<String, Template> templates = new HashMap<>();
    public static HashMap<String, Wrapper> wrappers = new HashMap<>();

    public static void main(String args[]) {
        Reader rd = new Reader();

        System.out.println("[Enable progress] Welcome to mc1cloud. Cloud is starting...");
        System.out.println("[Enable progress] Connecting to Database...");
        mysql_main = new MySQL("localhost", 3306, "cloud", "root", "", "cloudmaster");
        mysql_main.connect();

        System.out.println("[Enable progress] Creating necessary tables if not exists...");
        mysql_main.createCloudTables();

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

}
