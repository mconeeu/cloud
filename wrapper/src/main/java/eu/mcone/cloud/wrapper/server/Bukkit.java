/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.network.packet.ServerProgressStatePacketMaster;
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
        this.sendProgressState(ServerProgressStatePacketMaster.Progress.INPROGRESSING);
        this.runtime = Runtime.getRuntime();

        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final File serverDir = new File(homeDir + s + "wrapper" + s + "servers" + s + info.getName());
        final File templateZip = new File(serverDir + s + info.getTemplateName() + ".zip");

        System.out.println("[Server.class] Starting new server with the UUID: '" + info.getUuid() + "', Template '" + info.getTemplateName() + "', '" + info.getRam() + "gb ram, on the port '" + info.getPort() + "'...");

        if (serverDir.exists()) serverDir.delete();
        serverDir.mkdir();

        new Thread(() -> {
            try {
                System.out.println("[Server.class] Downloading Template...");
                //URL website = new URL("http://templates.mcone.eu/"+info.getTemplateName()+".zip");
                //FileOutputStream fos = new FileOutputStream(templateZip);
                //fos.getChannel().transferFrom(Channels.newChannel(website.openStream()), 0, Long.MAX_VALUE);

                System.out.println("[Server.class] Unzipping Template...");
                //System.out.println("new UnZip("+templateZip.getPath()+", "+serverDir.getPath()+");");
                //new UnZip(templateZip.getPath(), serverDir.getPath());
                //templateZip.delete();

                //createConsoleLogDirectory();

                setConfigs();

                String[] command = new String[]{"java",
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
                        serverDir + s + "server.jar"};

                this.process = this.runtime.exec(command, null, serverDir);

                //Register all Output for Spigot console
                new BukkitInputReader(this, true);

                this.process.waitFor();
                this.process.destroy();
            } catch (IOException | InterruptedException e) {
                System.err.println("[Server.class] Could not start server " + info.getName() + ":");
                if (e instanceof FileNotFoundException) {
                    System.err.println("[Server.class] Template does not exist, cancelling...");
                    return;
                }

                e.printStackTrace();
            }
        }).start();
        System.out.println("[Server.class] Server start of " + info.getName() + " initialised, method returned");
    }

    @Override
    public void stop() {
        if (process != null) {
            if (process.isAlive()) {
                System.out.println("[Server.class] Stopping the server " + this.info.getName() + "...");
                this.sendCommand("stop");
                this.info.setState(ServerState.OFFLINE);
                this.sendResult("[Server." + this.info.getName() + "] the server was stopped...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            } else {
                System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be stopped because the process is dead...");
                this.sendResult("[Server." + this.info.getName() + "] The server cloud not be stopped because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } else {
            System.out.println("[Serevr.class] The server '" + this.info.getName() + "' could not be stopped because it has no process...");
            this.sendResult("[Server." + this.info.getName() + "] The server could not be stopped because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
        }
    }

    private void setConfigs() throws IOException {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final String serverName = info.getName();
        final File propertyFile = new File(homeDir + s + "wrapper" + s + "servers" + s + serverName + s + "server.properties");
        final File spigotFile = new File(homeDir + s + "wrapper" + s + "servers" + s + serverName + s + "spigot.yml");
        final File bukkitFile = new File(homeDir + s + "wrapper" + s + "servers" + s + serverName + s + "bukkit.yml");

        if (!propertyFile.exists()) {
            propertyFile.createNewFile();
        }

        /*
         * server.properties
         */
        System.out.println("[Server.class] Set all server properties for server " + serverName + "...");
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
            System.out.println("[Server.class] Set all spigot.yml settings for server " + serverName + "...");
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
        System.out.println("[Server.class] Set all bukkit.yml settings for server " + serverName + "...");
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
