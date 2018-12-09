/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.console.BungeeInputReader;
import lombok.extern.java.Log;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Log
public class BungeeCord extends Server {

    public BungeeCord(ServerInfo info) {
        super(info);
    }

    @Override
    public void start() {
        this.initialise(
                calculatePort(),
                BungeeInputReader.class,
                new String[]{
                        "java",
                        "-DDisableTsQuery=true",
                        "-Dfile.encoding=UTF-8",
                        "-Djline.terminal=jline.UnsupportedTerminal",
                        "-Xmx"+info.getRam()+"M",
                        "-jar",
                        serverDir+File.separator+"server.jar"
                }
        );
    }

    @Override
    public void doStop() {
        this.sendCommand("end");
    }

    @Override
    void setConfig() throws IOException {
        final File propertyFile = new File(serverDir+File.separator+"server.properties");
        final File configFile = new File(serverDir+File.separator+"config.yml");

        /*
         * server.properties
         */
        if (!propertyFile.exists()) {
            propertyFile.createNewFile();
        }
        log.info("["+info.getName()+"] Setting all server properties...");
        Properties ps = new Properties();
        FileInputStream fisProperties = new FileInputStream(propertyFile);
        ps.load(fisProperties);

        //Server Data
        ps.setProperty("wrapper-ip", WrapperServer.getInstance().getHostname());
        ps.setProperty("server-port", Integer.toString(info.getPort()));
        ps.setProperty("max-players", Integer.toString(info.getMaxPlayers()));
        ps.setProperty("server-uuid", info.getUuid().toString());
        ps.setProperty("server-name", info.getName());

        FileOutputStream fosProperties = new FileOutputStream(propertyFile);
        ps.store(fosProperties, "MCONE_WRAPPER");

        fisProperties.close();
        fosProperties.close();


        /*
         * config.yml
         */
        log.info("["+info.getName()+"] Setting all config.yml settings!");
        if (!configFile.exists()) {
            URL fileUrl = getClass().getResource("/bungeeconfig.yml");
            FileUtils.copyURLToFile(fileUrl, configFile);
        }

        FileInputStream fisBungee = new FileInputStream(configFile);
        Configuration bungeeConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fisBungee);

        bungeeConf.set("online_mode", true);
        bungeeConf.set("ip_forward", true);

        List<?> listeners = bungeeConf.getList("listeners");

        HashMap<Object, Object> values = (listeners != null && listeners.size() > 0) ? (HashMap<Object, Object>) listeners.get(0) : new HashMap<>();
        values.put("host", "0.0.0.0:"+info.getPort());
        values.put("max_players", info.getMaxPlayers());
        values.put("motd", "&f&lMC ONE &3CloudServer &8» &7"+info.getName());

        List<Map<Object, Object>> result = new ArrayList<>();
        result.add(values);
        bungeeConf.set("listeners", result);

        ConfigurationProvider.getProvider(YamlConfiguration.class).save(bungeeConf, configFile);
        fisBungee.close();
    }

    private static int calculatePort() {
        int port = 25564;

        for (Server server : WrapperServer.getInstance().getServers()) {
            if (!server.getState().equals(ServerState.OFFLINE) && server.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                port = server.getInfo().getPort();
            }
        }

        //return getNextAvailablePort(++port);
        return ++port;
    }

}
