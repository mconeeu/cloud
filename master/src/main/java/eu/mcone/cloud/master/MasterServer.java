/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master;

import com.google.gson.Gson;
import eu.mcone.cloud.core.packet.*;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.console.ConsoleCommandExecutor;
import eu.mcone.cloud.master.handler.*;
import eu.mcone.cloud.master.network.WebRequestGetHandler;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.server.ServerManager;
import eu.mcone.cloud.master.server.StaticServerManager;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.api.ModuleHost;
import eu.mcone.networkmanager.api.module.NetworkModule;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import eu.mcone.networkmanager.core.api.database.Database;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bson.Document;

import java.util.*;

@Log
public class MasterServer extends NetworkModule {

    @Getter
    private static MasterServer instance;

    @Getter
    private ServerManager serverManager;
    @Getter
    private StaticServerManager staticServerManager;
    @Getter
    private Gson gson;
    @Getter
    private List<Template> templates = new ArrayList<>();
    @Getter
    private List<Wrapper> wrappers = new ArrayList<>();

    public void onLoad() {
        instance = this;

        registerPacket(ServerChangeStatePacketWrapper.class);
        registerPacket(ServerCommandExecutePacketWrapper.class);
        registerPacket(ServerInfoPacket.class);
        registerPacket(ServerListUpdatePacketPlugin.class);
        registerPacket(ServerPlayerCountUpdatePacketPlugin.class, new ServerPlayerCountUpdateHandler());
        registerPacket(ServerRegisterPacketPlugin.class, new ServerRegisterHandler());
        registerPacket(ServerUpdateStatePacket.class, new ServerUpdateStateHandler());
        registerPacket(WrapperRegisterFromStandalonePacketWrapper.class, new WrapperRegisterFromStandaloneHandler());
        registerPacket(WrapperRegisterPacketWrapper.class, new WrapperRegisterHandler());
        registerPacket(WrapperRequestPacketMaster.class, new WrapperRequestHandler());
        registerPacket(WrapperShutdownPacketWrapper.class);

        //registerWebRequestHandler("cloud", new WebRequestGetHandler());
    }

    public void onEnable() {
        ModuleHost.getInstance().getConsoleReader().registerCommand(new ConsoleCommandExecutor());
        gson = new Gson();

        log.info("Enable progress - " + ConsoleColor.AQUA + "Welcome to mc1cloud. CloudMaster is starting...");

        log.info("Enable progress - Getting templates from database...");
        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_templates").find()) {
            log.info("Enable progress - Creating Template " + entry.getString("name") + " and it's servers ...");

            createTemplate(
                    entry.getString("name"),
                    entry.getLong("ram"),
                    entry.getInteger("max_players"),
                    entry.getInteger("min"),
                    entry.getInteger("max"),
                    entry.getInteger("empty_servers"),
                    ServerVersion.valueOf(entry.getString("version")),
                    entry.get("properties", Document.class).toJson()
            );
        }

        log.info("Enable progress - Starting static server manager...");
        staticServerManager = new StaticServerManager();

        log.info("Enable progress - Starting ServerManager with TimeTask...");
        serverManager = new ServerManager();

        log.info("Enable progress - "+ConsoleColor.GREEN + "Enable process finished! Cloud Master seems to be ready! Waiting for connections...\n");
    }

    public void reload() {
        log.info("Reload progress - Reloading MasterServer...");
        log.info("Reload progress - Reloading Templates...");

        Map<String, Template> oldTemplates = new HashMap<>();
        List<String> newTemplates = new ArrayList<>();

        templates.forEach(t -> oldTemplates.put(t.getName(), t));

        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.CLOUD).getCollection("cloudmaster_templates").find()) {
            if (oldTemplates.containsKey(entry.getString("name"))) {
                log.info("Reload progress - Refreshing Template " + entry.getString("name") + "...");

                oldTemplates.get(entry.getString("name")).recreate(
                        entry.getInteger("ram"),
                        entry.getInteger("max_players"),
                        entry.getInteger("min"),
                        entry.getInteger("max"),
                        entry.getInteger("emptyservers"),
                        ServerVersion.valueOf(entry.getString("version")),
                        entry.get("properties", Document.class).toJson()
                );
            } else {
                log.info("Reload progress - Adding Template " + entry.getString("name") + "...");

                createTemplate(
                        entry.getString("name"),
                        entry.getInteger("ram"),
                        entry.getInteger("max_players"),
                        entry.getInteger("min"),
                        entry.getInteger("max"),
                        entry.getInteger("emptyservers"),
                        ServerVersion.valueOf(entry.getString("version")),
                        entry.get("properties", Document.class).toJson()
                );
            }

            newTemplates.add(entry.getString("name"));
        }

        for (Template t : templates) {
            if (!newTemplates.contains(t.getName())) {
                log.info("Reload progress - Deleting old Template " + t.getName() + "...");
                t.delete();
            }
        }

        log.info("Reload progress - Reloading static Servers...");
        staticServerManager.reload();

        log.info("Reload progress - "+ConsoleColor.GREEN + "MasterServer successfully reloaded!");
    }

    public void onDisable() {
        log.info("Shutdown progress - Shutting down ServerManager");
        serverManager.shutdown();

        log.info("Shutdown progress - The following Wrappers will stay online: " + getWrappers().size());
        for (Wrapper w : getWrappers()) {
            log.info("Shutdown progress - " + w.getUuid());
        }

        log.info("Shutdown progress - Stopping instance...");
        log.info("Shutdown progress - Good bye!");
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
