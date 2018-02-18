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
import eu.mcone.cloud.wrapper.server.console.BukkitInputReader;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Bukkit extends Server {

    public Bukkit(ServerInfo info) {
        super(info, calculatePort());
    }

    @Override
    public void start() {
        this.runtime = Runtime.getRuntime();

        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final File serverDir = new File(homeDir+s+"wrapper"+s+"servers"+s+info.getName());

        this.initialise(
                serverDir,
                BukkitInputReader.class,
                new String[]{"java",
                "-Dfile.encoding=UTF-8",
                "-jar",
                "-XX:+UseG1GC",
                "-XX:MaxGCPauseMillis=50",
                "-XX:-UseAdaptiveSizePolicy",
                "-Dcom.mojang.eula.agree=true",
                "-Dio.netty.recycler.maxCapacity=0 ",
                "-Dio.netty.recycler.maxCapacity.default=0",
                "-Djline.terminal=jline.UnsupportedTerminal",
                "-Xmx"+info.getRam()+"M",
                serverDir+s+"server.jar"}
        );
    }

    @Override
    public void stop() {
        if (process != null) {
            if (process.isAlive()) {
                Logger.log(getClass(), "["+info.getName()+"] Stopping server...");
                this.sendCommand("stop");
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
        final File spigotFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"spigot.yml");
        final File bukkitFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"bukkit.yml");

        if (!propertyFile.exists()) {
            propertyFile.createNewFile();
        }

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
        ps.setProperty("online-mode", "false");
        ps.setProperty("server-ip", WrapperServer.getInstance().getHostname());
        ps.setProperty("server-port", Integer.toString(info.getPort()));
        ps.setProperty("max-players", Integer.toString(info.getMaxPlayers()));
        ps.setProperty("motd", "\u00A7f\u00A7lMC ONE \u00A73Server \u00A78» \u00A77" + serverName);

        //CloudSystem Data
        ps.setProperty("server-uuid", info.getUuid().toString());
        ps.setProperty("server-templateID", Integer.toString(info.getTemplateID()));
        ps.setProperty("server-name", serverName);

        OutputStream outputstream = Files.newOutputStream(Paths.get(propertyFile.getPath()));
        outputstream.flush();
        ps.store(outputstream, "MCONE_WRAPPER");


        if (info.getVersion().equals(ServerVersion.SPIGOT)) {
            /*
             * spigot.yml
             */
            if (!spigotFile.exists()) {
                spigotFile.createNewFile();
            }

            Logger.log(getClass(), "["+info.getName()+"] Setting all spigot.yml settings...");
            final InputStreamReader isrSpigot = new InputStreamReader(Files.newInputStream(Paths.get(spigotFile.getPath())), StandardCharsets.UTF_8);
            final Configuration spigotConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(isrSpigot);

            Configuration sectionSettings = spigotConf.getSection("settings");
            sectionSettings.set("bungeecord", true);

            Configuration sectionMessages = spigotConf.getSection("messages");
            sectionMessages.set("whitelist", "§7§oDu stehst auf diesem Server nicht in der Whitelist!");
            sectionMessages.set("unknown-command", "§8[§7§l!§8] §4Dieser Befehl existiert nicht!");
            sectionMessages.set("server-full", "§7§oDer Server ist voll");
            sectionMessages.set("outdated-client", "§7§oBitte verwende die Minecraft Version {0}");
            sectionMessages.set("outdated-server", "§7§oBitte verwende die Minecraft Version {0}");
            sectionMessages.set("restart", "§7§oDer Server startet neu...");

            OutputStreamWriter oswSpigot = new OutputStreamWriter(Files.newOutputStream(Paths.get(spigotFile.getPath())), StandardCharsets.UTF_8);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(spigotConf, oswSpigot);
        }

        /*
         * bukkit.yml
         */
        if (!bukkitFile.exists()) {
            bukkitFile.createNewFile();
        }

        Logger.log(getClass(), "["+info.getName()+"] Setting all bukkit.yml settings...");
        final InputStreamReader isrBukkit = new InputStreamReader(Files.newInputStream(Paths.get(bukkitFile.getPath())), StandardCharsets.UTF_8);
        final Configuration bukkitConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(isrBukkit);

        Configuration sectionBukkitSettings = bukkitConf.getSection("settings");
        sectionBukkitSettings.set("shutdown-message", "§7§oDer Server startet neu...");

        System.out.println("[Server.class] Done all server.properties have been set...");
        this.sendResult("[Server." + serverName + "] Done all server.properties have been set...", ServerResultPacketWrapper.Result.SUCCESSFUL);

        OutputStreamWriter oswBukkit = new OutputStreamWriter(Files.newOutputStream(Paths.get(spigotFile.getPath())), StandardCharsets.UTF_8);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(bukkitConf, oswBukkit);
    }

    private static int calculatePort() {
        int port = 4000;

        for (Server server : WrapperServer.getInstance().getServers()) {
            port = server.getInfo().getPort();
        }

        port++;
        return port;
    }

}
