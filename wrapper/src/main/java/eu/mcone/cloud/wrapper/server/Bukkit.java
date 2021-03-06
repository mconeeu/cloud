/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.console.BukkitInputReader;
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
import java.util.Properties;

@Log
public class Bukkit extends Server {

    public Bukkit(ServerInfo info) {
        super(info);
    }

    @Override
    public void start() {
        this.initialise(
                calculatePort(),
                BukkitInputReader.class,
                new String[]{
                        "java",
                        "-Dfile.encoding=UTF-8",
                        "-jar",
                        "-XX:+UseG1GC",
                        "-XX:MaxGCPauseMillis=50",
                        "-XX:-UseAdaptiveSizePolicy",
                        "-Dcom.mojang.eula.agree=true",
                        "-Dio.netty.recycler.maxCapacity=0 ",
                        "-Dio.netty.recycler.maxCapacity.default=0",
                        "-Djline.terminal=jline.UnsupportedTerminal",
                        "-Xmx" + info.getRam() + "M",
                        serverDir + File.separator + "server.jar"
                }
        );
    }

    @Override
    public void doStop() {
        this.sendCommand("stop");
    }

    @Override
    void setConfig() throws IOException {
        final File propertyFile = new File(serverDir + File.separator + "server.properties");
        final File spigotFile = new File(serverDir + File.separator + "spigot.yml");
        final File bukkitFile = new File(serverDir + File.separator + "bukkit.yml");

        /*
         * server.properties
         */
        if (!propertyFile.exists()) {
            URL fileUrl = getClass().getResource("/server.properties");
            FileUtils.copyURLToFile(fileUrl, propertyFile);
        }
        log.info("[" + info.getName() + "] Setting all server properties...");
        Properties ps = new Properties();
        FileInputStream fis = new FileInputStream(propertyFile);
        ps.load(fis);

        //Server Data
        ps.setProperty("online-mode", "false");
        ps.setProperty("server-ip", "0.0.0.0");
        ps.setProperty("wrapper-ip", WrapperServer.getInstance().getHostname());
        ps.setProperty("server-port", Integer.toString(info.getPort()));
        ps.setProperty("max-players", Integer.toString(info.getMaxPlayers()));
        ps.setProperty("motd", "\u00A7f\u00A7lMC ONE \u00A73CloudServer \u00A78?? \u00A77" + info.getName());

        //CloudSystem Data
        ps.setProperty("server-uuid", info.getUuid().toString());
        ps.setProperty("wrapper-uuid", WrapperServer.getInstance().getWrapperUuid().toString());
        ps.setProperty("server-templateID", Integer.toString(info.getTemplateID()));
        ps.setProperty("server-name", info.getName());
        ps.setProperty("server-version", info.getVersion().toString());
        ps.setProperty("static-server", Boolean.valueOf(info.isStaticServer()).toString());

        FileOutputStream fos = new FileOutputStream(propertyFile);
        ps.store(fos, "MCONE_Wrapper");
        fos.close();
        fis.close();


        if (info.getVersion().equals(ServerVersion.SPIGOT)) {
            /*
             * spigot.yml
             */
            if (!spigotFile.exists()) {
                URL fileUrl = getClass().getResource("/spigot.yml");
                FileUtils.copyURLToFile(fileUrl, spigotFile);
            }

            log.info("[" + info.getName() + "] Setting all spigot.yml settings...");
            final Configuration spigotConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(spigotFile);

            Configuration sectionSettings = spigotConf.getSection("settings");
            sectionSettings.set("bungeecord", true);

            Configuration sectionMessages = spigotConf.getSection("messages");
            sectionMessages.set("whitelist", "??7??oDu stehst auf diesem Server nicht in der Whitelist!");
            sectionMessages.set("unknown-command", "??8[??7??l!??8] ??4Dieser Befehl existiert nicht!");
            sectionMessages.set("server-full", "??7??oDer Server ist voll");
            sectionMessages.set("outdated-client", "??7??oBitte verwende die Minecraft Version {0}");
            sectionMessages.set("outdated-server", "??7??oBitte verwende die Minecraft Version {0}");
            sectionMessages.set("restart", "??7??oDer Server startet neu...");

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(spigotConf, spigotFile);
        }

        /*
         * bukkit.yml
         */
        if (!bukkitFile.exists()) {
            URL fileUrl = getClass().getResource("/bukkit.yml");
            FileUtils.copyURLToFile(fileUrl, bukkitFile);
        }

        log.info("[" + info.getName() + "] Setting all bukkit.yml settings...");
        final Configuration bukkitConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(bukkitFile);

        Configuration sectionBukkitSettings = bukkitConf.getSection("settings");
        sectionBukkitSettings.set("shutdown-message", "??7??oDer Server startet neu...");

        ConfigurationProvider.getProvider(YamlConfiguration.class).save(bukkitConf, bukkitFile);
    }

    private static int calculatePort() {
        int port = 4000;

        for (Server server : WrapperServer.getInstance().getServers()) {
            if (!server.getState().equals(ServerState.OFFLINE) && !server.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                port = server.getInfo().getPort();
            }
        }

        //return getNextAvailablePort(++port);
        return ++port;
    }

}
