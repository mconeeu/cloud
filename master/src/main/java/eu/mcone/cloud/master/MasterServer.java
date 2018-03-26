/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master;

import eu.mcone.cloud.core.console.ConsoleReader;
import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.mysql.MySQL;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.console.CommandExecutor;
import eu.mcone.cloud.master.network.ServerBootstrap;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.server.ServerManager;
import eu.mcone.cloud.master.server.StaticServerManager;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import io.netty.channel.Channel;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MasterServer {

    @Getter
    private static MasterServer instance;
    @Getter
    private MySQL mysql;

    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private ServerManager serverManager;
    @Getter
    private StaticServerManager staticServerManager;
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
        createMySQLTables(mysql);

        System.out.println("[Enable progress] Getting wrappers from database...");
        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_wrappers;", rs -> {
            try {
                while (rs.next()) {
                    System.out.println("[Enable progress] Creating Wrapper " + rs.getString("uuid") + " with adress "+rs.getString("adress")+" ...");
                    createWrapper(UUID.fromString(rs.getString("uuid")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        System.out.println("[Enable progress] Getting templates from database...");
        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_templates;", rs -> {
            try {
                while (rs.next()) {
                    System.out.println("[Enable progress] Creating Template " + rs.getString("name") + " and it's servers ...");
                    createTemplate(
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
        staticServerManager = new StaticServerManager(mysql);

        System.out.println("[Enable progress] Starting ServerManager with TimeTask...");
        serverManager = new ServerManager();

        System.out.println("[Enable progress] Starting Netty Server...");
        new ServerBootstrap(4567);

        System.out.println("\nEnable process finished! Cloud Master seems to be ready! Waiting for connections...");
    }

    public void shutdown() {
        Logger.log("Shutdown progress", "Shutting down ServerManager");
        serverManager.shutdown();

        Logger.log("Shutdown progress", "The following Wrappers will stay online: "+getWrappers().size());
        for (Wrapper w : getWrappers()) {
            Logger.log("Shutdown progress", " -"+w.getUuid());
        }

        System.out.println("[Shutdowm progress] Stopping instance...");
        System.out.println("[Shutdowm progress] Good bye!");
        System.exit(0);
    }

    private void createMySQLTables(MySQL mySQL) {
        mySQL.update("CREATE TABLE IF NOT EXISTS `" + mySQL.getTablePrefix() + "_templates` " +
                "(" +
                "`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`name` varchar(100) NOT NULL UNIQUE KEY, " +
                "`max_players` int(5) NOT NULL, " +
                "`ram` int(8) NOT NULL, " +
                "`min` int(5) NOT NULL, " +
                "`max` int(5) NOT NULL, " +
                "`version` varchar(10) NOT NULL, " +
                "`update` int(5) NOT NULL, " +
                "`emptyservers` int(5) NOT NULL, " +
                "`startup` boolean NOT NULL" +
                ") " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8;");

        mySQL.update("CREATE TABLE IF NOT EXISTS `" + mySQL.getTablePrefix() + "_static_servers` " +
                "(" +
                "`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`name` varchar(100) NOT NULL, `max` int(5) NOT NULL, " +
                "`ram` int(8) NOT NULL, " +
                "`version` varchar(10) NOT NULL, " +
                "`wrapper` varchar(100)" +
                ") " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8;");

        mySQL.update("CREATE TABLE IF NOT EXISTS `" + mySQL.getTablePrefix() + "_wrappers` " +
                "(" +
                "`id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`uuid` varchar(100) NOT NULL, " +
                "`adress` varchar(100) NOT NULL" +
                ") " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8;");
    }

    private void createTemplate(String name, long ram, int maxPlayers, int min, int max, int emptyservers, ServerVersion version, boolean startup) {
        templates.add(new Template(name, ram, maxPlayers, min, max, emptyservers, version, startup));
    }

    public Wrapper createWrapper(UUID uuid) {
        Wrapper w = new Wrapper(uuid);

        wrappers.add(w);
        return w;
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

    public Server getServer(String name) {
        for (Template t : templates) {
            for (Server s : t.getServers()) {
                if (s.getInfo().getName().equals(name)) {
                    return s;
                }
            }
        }
        return null;
    }

    public Wrapper getWrapper(Channel channel) {
        for (Wrapper w : wrappers) {
            if (w.getChannel() != null && w.getChannel().equals(channel)) {
                return w;
            }
        }
        return null;
    }

    public Wrapper getWrapper(UUID uuid) {
        for (Wrapper w : wrappers) {
            if (w.getUuid().equals(uuid)) {
                return w;
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
