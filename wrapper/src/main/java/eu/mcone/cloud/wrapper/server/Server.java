/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.file.UnZip;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    @Getter @Setter
    private ServerInfo info;
    @Getter
    private ProcessBuilder processBuilder;
    @Getter
    private Process process;

    public Server(ServerInfo info) {
        this.info = info;
        this.getInfo().setPort(calculatePort());
        WrapperServer.getInstance().getServers().add(this);

        System.out.println("[Server.class] New Server " + this.info.getName() + " initialized! Creating Directories...");
        /* ... */

        if (this.info.getTemplateName() != null) {
            System.out.println("[Server.class] Downloading template " + this.info.getTemplateName() + " for Server " + this.info.getName() + "...");
            /* ... */

        } else {
            System.out.println("[Server.class] No template set for Server " + this.info.getName() + "! Starting Server...");
        }
    }

    public void start() {
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
                URL website = new URL("http://templates.mcone.eu/"+info.getTemplateName()+".zip");
                FileOutputStream fos = new FileOutputStream(templateZip);
                fos.getChannel().transferFrom(Channels.newChannel(website.openStream()), 0, Long.MAX_VALUE);

                System.out.println("[Server.class] Unzipping Template...");
                System.out.println("new UnZip("+templateZip.getPath()+", "+serverDir.getPath()+");");
                new UnZip(templateZip.getPath(), serverDir.getPath());
                templateZip.delete();

                setServerProperties();
                //createConsoleLogDirectory();

                processBuilder = new ProcessBuilder()
                        .inheritIO()
                        .command("java",
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
                                serverDir+s+"server.jar"
                        )
                        .directory(serverDir)
                        .redirectErrorStream(true)
                        .redirectInput(ProcessBuilder.Redirect.PIPE)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE);

                this.process = this.processBuilder.start();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.flush();

                //Register all Output for Spigot console
                new ConsoleInputReader(this);

                this.process.waitFor();
            } catch (IOException | InterruptedException e) {
                System.err.println("[Server.class] Could not start server "+info.getName()+":");
                if (e instanceof FileNotFoundException) {
                    System.err.println("[Server.class] Template does not exist, cancelling...");
                    return;
                }

                e.printStackTrace();
            }
        }).start();
    }

    private void setServerProperties() throws IOException {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final String serverName = info.getName();
        final File propertyFile = new File(homeDir+s+"wrapper"+s+"servers"+s+serverName+s+"server.properties");

        if (!propertyFile.exists()) {
            propertyFile.createNewFile();
        }

        System.out.println("[Server.class] Set all server properties for server " + serverName + "...");
        Properties ps = new Properties();

        InputStreamReader inputstreamreader = new InputStreamReader(Files.newInputStream(Paths.get(propertyFile.getPath())));
        ps.load(inputstreamreader);

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
        ps.store(outputstream, "MCONE_WRAPPER");

        System.out.println("[Server.class] Done all server.properties have been set...");
        this.sendResult("[Server." + serverName + "] Done all server.properties have been set...", ServerResultPacketWrapper.Result.SUCCESSFUL);
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

    private void sendResult(String message, ServerResultPacketWrapper.Result result) {
        try {
            WrapperServer.getInstance().send(new ServerResultPacketWrapper("Server.class", message, result));
            System.out.println("[Server.class] The result '" + message + "\\" + result.toString() + "' was sent to the master...");
        } catch (Exception e) {
            System.out.println("[Server.class] The result could not be sent to the master...");
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    System.out.println("[Server.class] Stopping the server " + this.info.getName() + "...");
                    this.sendcommand("stop");
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
        while (WrapperServer.getInstance().getServers().iterator().hasNext())
            port = WrapperServer.getInstance().getServers().iterator().next().getInfo().getPort();

        port++;
        return port;
    }

}
