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
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    @Getter @Setter
    private ServerInfo info;
    @Getter
    private Process process;
    @Getter
    public Runtime rt;

    public Server(ServerInfo info) {
        this.info = info;
        this.getInfo().setPort(info.getVersion().equals(ServerVersion.BUNGEE) ? calculateProxyPort() : calculatePort());
        WrapperServer.getInstance().getServers().add(this);
    }

    public void start() {
        this.sendProgressState(ServerProgressStatePacketMaster.Progress.INPROGRESSING);
        this.rt = Runtime.getRuntime();

        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final File serverDir = new File(homeDir+s+"wrapper"+s+"servers"+s+info.getName());
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

                if (info.getVersion().equals(ServerVersion.BUNGEE)) {
                    setBungeeConfig();

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

                    this.process = this.rt.exec(command, null, serverDir);

                    //Register all Output for Spigot console
                    new ConsoleInputReaderBungee(this, true);
                } else if (info.getVersion().equals(ServerVersion.SPIGOT) || info.getVersion().equals(ServerVersion.BUKKIT)) {
                    setSpigotConfig();


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
                            "-Xmx"+info.getRam()+"M",
                            serverDir+s+"server.jar"};

                    this.process = this.rt.exec(command, null, serverDir);

                    //Register all Output for Spigot console
                    new ConsoleInputReaderServer(this, true);
                }

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.flush();

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

    private void setSpigotConfig() throws IOException {
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

        OutputStream os = Files.newOutputStream(Paths.get(spigotFile.getPath()));
        os.flush();

        /*
         * bukkit.yml
         */
        System.out.println("[Server.class] Set all spigot.yml settings for server " + serverName + "...");
        final InputStreamReader isrBukkit = new InputStreamReader(Files.newInputStream(Paths.get(bukkitFile.getPath())), StandardCharsets.UTF_8);
        final Configuration bukkitConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(isrBukkit);

        Configuration sectionBukkitSettings = bukkitConf.getSection("settings");
        sectionBukkitSettings.set("shutdown-message", "§7§oDer Server startet neu...");

        System.out.println("[Server.class] Done all server.properties have been set...");
        this.sendResult("[Server." + serverName + "] Done all server.properties have been set...", ServerResultPacketWrapper.Result.SUCCESSFUL);
    }

    private void setBungeeConfig() throws IOException {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final String serverName = info.getName();
        final File configFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"config.yml");

        System.out.println("[Server.class] Set all spigot.yml settings for server " + serverName + "...");
        final InputStreamReader isrSpigot = new InputStreamReader(Files.newInputStream(Paths.get(configFile.getPath())), StandardCharsets.UTF_8);
        final Configuration bungeeConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(isrSpigot);

        bungeeConf.set("ip_forward", true);
        bungeeConf.set("online_mode", true);

        Configuration sectionSettings = bungeeConf.getSection("listeners");
        sectionSettings.set("host", "0.0.0.0:"+info.getPort());
        sectionSettings.set("max_players", info.getMaxPlayers());
    }

    private void createConsoleLogDirectory(){
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final String name = info.getName();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String currentDate = dateFormat.format(date).replace(":", ".");

        File mc1cloud_directory = new File(homeDir + "\\wrapper\\servers\\" + name + "\\mc1cloud");
        File errorlog = new File(homeDir + "\\wrapper\\servers\\" + name + "\\mc1cloud\\Console-" + currentDate + ".yml");

        Properties ps = new Properties();

        try{
            System.out.println("[Server.class] Creating ConsoleLog directory and file for server " + name + "...");

            if(errorlog.exists() || !(errorlog.exists()) ){
                if(!(mc1cloud_directory.exists())) {
                    mc1cloud_directory.mkdir();
                }

                errorlog.createNewFile();
            }

            System.out.println("[Server.class] Set all ConfigLog values for server " + name + "...");

            ps.setProperty("server-name", name);
            ps.setProperty("server-uuid", info.getUuid().toString());
            ps.setProperty("server-templateid", Integer.toString(info.getTemplateID()));
            ps.setProperty("server-port", Integer.toString(info.getPort()));
            ps.setProperty("server-maxplayers", Integer.toString(info.getMaxPlayers()));

            OutputStream outputstream = Files.newOutputStream(Paths.get(homeDir + "\\wrapper\\servers\\" + name + "\\mc1cloud\\Console-" + currentDate + ".yml"));
            ps.store(outputstream, "MCONE_WRAPPER");

            System.out.println("[Server.class] Done all ConsoleLog values have been set for server " + name + "...");
            this.sendResult("[Server." + name + "] Done all ConsoleLog values have been set...", ServerResultPacketWrapper.Result.SUCCESSFUL);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendcommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive() == Boolean.TRUE) {
                    BufferedWriter input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    input.write(command);
                    System.out.println("[Server.class] Sent command '" + command + "' to the server " + this.info.getName());
                    this.sendResult("[Server." + this.info.getName() + "] Sent command '" + command + "' to server...", ServerResultPacketWrapper.Result.COMMAND);
                } else {
                    System.out.println("[Server.class] The command '" + command + "' could not be sent to Serevr '" + this.info.getName() + "' because the process is dead...");
                    this.sendResult("[Server." + this.info.getName() + "] The command '" + command + "' could not be sent to server '" + this.info.getName() + "' because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
                }
            } else {
                System.out.println("[Serevr.class] The command '" + command + "' could not be sent to Server '" + this.info.getName() + "' because it has no process...");
                this.sendResult("[Server." + this.info.getName() + "] The command '" + command + "' could not be sent to server because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } catch (IOException e) {
            System.out.println("[Server.class] Error the command '" + command + "' could not be sent to the server " + this.info.getName());
            this.sendResult("[Server." + this.info.getName() + "] the command '" + command + "' cloud not be sent to server...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void sendResult(String message, ServerResultPacketWrapper.Result result) {
        try {
            WrapperServer.getInstance().send(new ServerResultPacketWrapper("Server.class", message, result));
            System.out.println("[Server.class] The result '" + message + "\\" + result.toString() + "' was sent to the master...");
        } catch (Exception e) {
            System.out.println("[Server.class] The result could not be sent to the master...");
            e.printStackTrace();
        }
    }

    public void sendProgressState(ServerProgressStatePacketMaster.Progress progress){
        try{
            WrapperServer.getInstance().send(new ServerProgressStatePacketMaster("Server.class", progress));
            System.out.println("[Server.class] Send new progress state '" + progress.toString()  +"' to server Master...");
        }catch (Exception e){
            System.out.println("[Server.clas] Could not be sent new progress state to server Master");
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    System.out.println("[Server.class] Stopping the server " + this.info.getName() + "...");
                    this.sendcommand(info.getVersion().equals(ServerVersion.BUKKIT) ? "stop" : "end");
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

    public void forceStop() {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    System.out.println("[Server.class] ForceStop server " + this.info.getName() + "...");
                    process.destroy();
                    info.setState(ServerState.OFFLINE);
                    sendResult("[Server." + this.info.getName() + "] The server was ForceStopped...", ServerResultPacketWrapper.Result.SUCCESSFUL);
                } else {
                    System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be ForeStopped because the process is dead...");
                    this.sendResult("[Server." + this.info.getName() + "] The server cloud not be ForeStopped because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
                }
            } else {
                System.out.println("[Serevr.class] The server '" + this.info.getName() + "' could not be ForeStopped because it has no process...");
                this.sendResult("[Server." + this.info.getName() + "] The server could not be ForeStopped because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } catch (Exception e) {
            System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be ForeStopped...");
            this.sendResult("[Server." + this.info.getName() + "] The server '" + this.info.getName() + "' could not be ForeStopped...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void restart() {
        this.info.setState(ServerState.OFFLINE);
        stop();
        this.info.setState(ServerState.STARTING);
        start();
        this.info.setState(ServerState.WAITING);
    }

    public void delete() {
        System.out.println("[Server.class] Deleting server " + this.info.getName() + "...");
        if (process.isAlive() == Boolean.TRUE) {
            String server_name = this.info.getName();

            this.forceStop();
            WrapperServer.getInstance().getServers().remove(this);

            System.out.println("[Server.class] The server " + server_name + " was deleted...");
            this.sendResult("[Server." + server_name + "] The server was deleted...", ServerResultPacketWrapper.Result.SUCCESSFUL);
        } else {
            String server_name = this.info.getName();
            WrapperServer.getInstance().getServers().remove(this);

            System.out.println("[Server.class] The server " + server_name + " was deleted...");
            this.sendResult("[Server." + server_name + "] The server was deleted...", ServerResultPacketWrapper.Result.SUCCESSFUL);
        }
    }

    private String isAlive() {
        String msg = null;

        if (this.process.isAlive() == Boolean.TRUE) {
            msg = "Process " + this.process.toString() + " is Alive";
            return msg;
        } else {
            msg = "Process " + this.process.toString() + " is not Alive";
            return msg;
        }
    }

    private int calculatePort() {
        int port = 4000;

        for (Server server : WrapperServer.getInstance().getServers()) {
            port = server.getInfo().getPort();
        }

        port++;
        return port;
    }

    private int calculateProxyPort() {
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
