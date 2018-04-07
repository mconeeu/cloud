/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master;

import eu.mcone.cloud.core.console.ConsoleColor;
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
import java.util.*;

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

        Logger.log("Enable progress", ConsoleColor.GREEN+"Welcome to mc1cloud. Cloud is starting...");
        Logger.log("Enable progress", ConsoleColor.GREEN+"Connecting to Database...");
        mysql = new MySQL("mysql.mcone.eu", 3306, "mc1cloud", "mc1cloud", "5CjLP5dHYXQPX85zPizx5hayz0AYNOuNmzcegO0Id0AXnp3w1OJ3fkEQxbGJZAuJ", "cloudmaster");

        Logger.log("Enable progress", ConsoleColor.GREEN+"Creating necessary tables if not exists...");
        createMySQLTables(mysql);

        Logger.log("Enable progress", ConsoleColor.GREEN+"Getting templates from database...");
        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_templates;", rs -> {
            try {
                while (rs.next()) {
                    Logger.log("Enable progress", ConsoleColor.GREEN+"Creating Template " + rs.getString("name") + " and it's servers ...");

                    createTemplate(
                            rs.getString("name"),
                            rs.getInt("ram"),
                            rs.getInt("max_players"),
                            rs.getInt("min"),
                            rs.getInt("max"),
                            rs.getInt("emptyservers"),
                            ServerVersion.valueOf(rs.getString("version")),
                            rs.getString("properties")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Logger.log("Enable progress", ConsoleColor.GREEN+"Starting static server manager...");
        staticServerManager = new StaticServerManager(mysql);

        Logger.log("Enable progress", ConsoleColor.GREEN+"Starting ServerManager with TimeTask...");
        serverManager = new ServerManager();

        Logger.log("Enable progress", ConsoleColor.GREEN+"Starting Netty Server...");
        new Thread(() -> new ServerBootstrap(4567)).start();

        Logger.log("Enable progress", ConsoleColor.GREEN+"Enable process finished! Cloud Master seems to be ready! Waiting for connections...");
    }

    public void reload() {
        Logger.log("Reload progress", ConsoleColor.GREEN+"Reloading MasterServer...");
        Logger.log("Reload progress", ConsoleColor.GREEN+"Reloading Templates...");
        mysql.select("SELECT * FROM " + mysql.getTablePrefix() + "_templates;", rs -> {
            Map<String, Template> oldTemplates = new HashMap<>();
            List<String> newTemplates = new ArrayList<>();

            templates.forEach(t -> oldTemplates.put(t.getName(), t));

            try {
                while (rs.next()) {
                    if (oldTemplates.containsKey(rs.getString("name"))) {
                        Logger.log("Reload progress", ConsoleColor.GREEN+"Refreshing Template " + rs.getString("name") + "...");

                        oldTemplates.get(rs.getString("name")).recreate(
                                rs.getInt("ram"),
                                rs.getInt("max_players"),
                                rs.getInt("min"),
                                rs.getInt("max"),
                                rs.getInt("emptyservers"),
                                ServerVersion.valueOf(rs.getString("version")),
                                rs.getString("properties")
                        );
                    } else {
                        Logger.log("Reload progress", ConsoleColor.GREEN+"Adding Template " + rs.getString("name") + "...");

                        createTemplate(
                                rs.getString("name"),
                                rs.getInt("ram"),
                                rs.getInt("max_players"),
                                rs.getInt("min"),
                                rs.getInt("max"),
                                rs.getInt("emptyservers"),
                                ServerVersion.valueOf(rs.getString("version")),
                                rs.getString("properties")
                        );
                    }

                    newTemplates.add(rs.getString("name"));
                }

                for (Template t : templates) {
                    if (!newTemplates.contains(t.getName())) {
                        Logger.log("Reload progress", ConsoleColor.GREEN+"Deleting old Template " + t.getName() + "...");
                        t.delete();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Logger.log("Reload progress", ConsoleColor.GREEN+"Reloading static Servers...");
        staticServerManager.reload();

        Logger.log("Reload progress", ConsoleColor.GREEN+"MasterServer successfully reloaded!");
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
                "`emptyservers` int(5) NOT NULL, " +
                "`properties` varchar(1000) NOT NULL " +
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
    }

    private void createTemplate(String name, long ram, int maxPlayers, int min, int max, int emptyservers, ServerVersion version, String properties) {
        templates.add(new Template(name, ram, maxPlayers, min, max, emptyservers, version, properties));
    }

    public void unregisterTemplate(Template t) {
        templates.remove(t);
    }

    public Wrapper createWrapper(UUID uuid, Channel channel, long ram) {
        Wrapper w = new Wrapper(uuid, channel, ram);

        wrappers.add(w);
        return w;
    }

    public void unregisterWrapper(Wrapper w) {
        wrappers.remove(w);
    }

    public Server getServer(UUID uuid) {
        for (Template t : templates) {
            for (Server s : t.getServers()) {
                if (s.getInfo().getUuid().equals(uuid)) {
                    return s;
                }
            }
        }
        for (Server s : staticServerManager.getServers()) {
            if (s.getInfo().getUuid().equals(uuid)) {
                return s;
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
        for (Server s : staticServerManager.getServers()) {
            if (s.getInfo().getName().equals(name)) {
                return s;
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
        result.addAll(staticServerManager.getServers());

        return result;
    }

}
