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
import eu.mcone.cloud.wrapper.server.console.BungeeInputReader;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BungeeCord extends Server {

    public BungeeCord(ServerInfo info) {
        super(info, calculatePort());
    }

    @Override
    public void start() {
        this.sendProgressState(ServerProgressStatePacketMaster.Progress.INPROGRESSING);
        this.runtime = Runtime.getRuntime();

        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final File serverDir = new File(homeDir+s+"wrapper"+s+"bungees"+s+info.getName());
        final File templateZip = new File(serverDir+s+info.getTemplateName()+".zip");

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

                //setConfig();

                String[] command = new String[]{"java",
                        "-Dfile.encoding=UTF-8",
                        "-jar",
                        "-XX:+UseG1GC",
                        "-XX:MaxGCPauseMillis=50",
                        "-XX:-UseAdaptiveSizePolicy",
                        "-Dio.netty.recycler.maxCapacity=0 ",
                        "-Dio.netty.recycler.maxCapacity.default=0",
                        "-Xmx"+info.getRam()+"M",
                        serverDir+s+"bungee.jar"};

                this.process = this.runtime.exec(command, null, serverDir);

                //Register all Output for bungeecord console
                new BungeeInputReader(this, true);

                this.process.waitFor();
                this.process.destroy();
            } catch (IOException | InterruptedException e) {
                System.err.println("[Server.class] Could not start server "+info.getName()+":");
                if (e instanceof FileNotFoundException) {
                    System.err.println("[Server.class] Template does not exist, cancelling...");
                    return;
                }

                e.printStackTrace();
            }
        }).start();
        System.out.println("[Server.class] Server start of "+info.getName()+" initialised, method returned");
    }

    @Override
    public void stop() {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    System.out.println("[Server.class] Stopping the server " + this.info.getName() + "...");
                    this.sendCommand("end");
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
        } catch (Exception e) {
            System.out.println("[WRAPPER] Error in method StopServer");
            e.printStackTrace();
        }
    }

    private void setConfig() throws IOException {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final String serverName = info.getName();
        final File configFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"config.yml");

        System.out.println("[Server.class] Set all spigot.yml settings for server " + serverName + "...");
        final InputStreamReader isrBungee = new InputStreamReader(Files.newInputStream(Paths.get(configFile.getPath())), StandardCharsets.UTF_8);
        final Configuration bungeeConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(isrBungee);

        bungeeConf.set("ip_forward", true);
        bungeeConf.set("online_mode", true);

        Configuration sectionSettings = bungeeConf.getSection("listeners");
        sectionSettings.set("host", "0.0.0.0:"+info.getPort());
        sectionSettings.set("max_players", info.getMaxPlayers());

        OutputStreamWriter oswSpigot = new OutputStreamWriter(Files.newOutputStream(Paths.get(configFile.getPath())), StandardCharsets.UTF_8);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(bungeeConf, oswSpigot);
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
