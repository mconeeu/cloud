/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.console.BungeeInputReader;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BungeeCord extends Server {

    public BungeeCord(ServerInfo info) {
        super(info, calculatePort());
    }

    @Override
    public void start() {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final File serverDir = new File(homeDir+s+"wrapper"+s+"servers"+s+info.getName());

        this.initialise(
                serverDir,
                BungeeInputReader.class,
                new String[]{
                        "java",
                        "-Dfile.encoding=UTF-8",
                        "-jar",
                        "-XX:+UseG1GC",
                        "-XX:MaxGCPauseMillis=50",
                        "-XX:-UseAdaptiveSizePolicy",
                        "-Dio.netty.recycler.maxCapacity=0 ",
                        "-Dio.netty.recycler.maxCapacity.default=0",
                        "-Xmx"+info.getRam()+"M",
                        serverDir+s+"bungee.jar"
                }
        );
    }

    @Override
    public void stop() {
        if (process != null) {
            if (process.isAlive()) {
                Logger.log(getClass(), "["+info.getName()+"] Stopping server...");
                this.sendCommand("end");
                this.setState(ServerState.OFFLINE);
                this.sendResult("[Server." + this.info.getName() + "] the server was stopped!", ServerResultPacketWrapper.Result.SUCCESSFUL);
            } else {
                Logger.err(getClass(), "["+info.getName()+"] Could not stop server because the process is dead!");
                this.sendResult("[Server." + this.info.getName() + "] The server cloud not be stopped because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } else {
            Logger.err(getClass(), "["+info.getName()+"] Could not stop server because it has no process!");
            this.sendResult("[Server." + this.info.getName() + "] The server could not be stopped because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
        }
    }

    @Override
    void setConfig() throws IOException {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final String serverName = info.getName();
        final File propertyFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"server.properties");
        final File configFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"config.yml");

        /*
         * server.properties
         */
        if (!propertyFile.exists()) {
            propertyFile.createNewFile();
        }
        Logger.log(getClass(), "["+info.getName()+"] Setting all server properties...");
        Properties ps = new Properties();
        final InputStreamReader isrProperties = new InputStreamReader(Files.newInputStream(Paths.get(propertyFile.getPath())));
        ps.load(isrProperties);

        //Server Data
        ps.setProperty("wrapper-ip", WrapperServer.getInstance().getHostname());
        ps.setProperty("server-port", Integer.toString(info.getPort()));
        ps.setProperty("max-players", Integer.toString(info.getMaxPlayers()));
        ps.setProperty("server-uuid", info.getUuid().toString());
        ps.setProperty("server-name", serverName);

        OutputStream outputstream = Files.newOutputStream(Paths.get(propertyFile.getPath()));
        outputstream.flush();
        ps.store(outputstream, "MCONE_WRAPPER");


        /*
         * config.yml
         */
        Logger.log(getClass(), "["+info.getName()+"] Setting all config.yml settings!");
        if (!configFile.exists()) {
            URL fileUrl = getClass().getResource("/bungeeconfig.yml");
            FileUtils.copyURLToFile(fileUrl, configFile);
        }

        final InputStreamReader isrBungee = new InputStreamReader(Files.newInputStream(Paths.get(configFile.getPath())), StandardCharsets.UTF_8);
        final Configuration bungeeConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(isrBungee);

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

        OutputStreamWriter oswBungee = new OutputStreamWriter(Files.newOutputStream(Paths.get(configFile.getPath())), StandardCharsets.UTF_8);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(bungeeConf, oswBungee);
    }

    private static int calculatePort() {
        int port = 25564;

        for (Server server : WrapperServer.getInstance().getServers()) {
            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                port = server.getInfo().getPort();
            }
        }

        port++;
        return port;
    }

}
