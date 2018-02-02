/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.directorymanager.CopyManager;
import eu.mcone.cloud.core.network.packet.ServerResultPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.system.OS;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.console.ConsoleErrorStreamReader;
import eu.mcone.cloud.wrapper.console.ConsoleInputReader;
import eu.mcone.cloud.wrapper.util.Var;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    @Getter
    @Setter
    private ServerInfo info;

    @Getter
    private ProcessBuilder pb;

    @Getter
    private Process p;

    @Getter
    private String current_date;

    private HashMap<UUID, Process> server_process = new HashMap<>();

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
        String server_name = info.getName();
        String tempalte = info.getTemplateName();
        int port = info.getPort();
        int ram = info.getRam();

        try {
            final File server_directory = new File(Var.getSystemPath() + "\\wrapper\\servers\\" + server_name);
            final File target = new File(Var.getSystemPath()  + "\\wrapper\\templates\\" + tempalte);
            final File copy_to = new File(Var.getSystemPath()  + "\\wrapper\\servers\\" + server_name + "\\");

            System.out.println("[Server.class] Starting new server with the UUID: '" + this.info.getUuid() + "', Template '" + this.info.getTemplateName() + "', '" + this.info.getRam() + "gb ram, on the port '" + this.info.getPort() + "'...");

            if (!(target.exists())) {
                System.out.println("[Server.class] Error there is no template for the server " + server_name.toString() + ", template name: " + tempalte.toString());
                this.sendResult("[Server.class] Error there is no template for the server " + server_name.toString() + ", template name: " + tempalte.toString(), ServerResultPacket.Result.ERROR);
                return;
            } else {
                CopyManager.copyFilesInDirectory(target, copy_to);
                System.out.println("Copy template in directory...");
                this.sendResult("[Server." + server_name + "] Copy template in directory...", ServerResultPacket.Result.INFORMATION);
            }

            System.out.println("[Server.class] Starting new process for the server  " + server_name + "...");
            this.sendResult("[Server." + server_name + "] Starting new porcess for the server...", ServerResultPacket.Result.INFORMATION);
            this.pb = new ProcessBuilder()
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
                            "-Xmx" + this.info.getRam() + "G",
                            server_directory + "\\spigot.jar",
                            "nogui")
                    .directory(server_directory)
                    .redirectErrorStream(true)
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE);

            this.p = this.pb.start();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.p.getOutputStream()));
            writer.flush();

            System.out.println("[Server.class] Set all config values for the server " + server_name + "...");
            this.SetConfigValues(server_directory);

            /* Unused */
            this.CreateConsoleLogDirectory(server_directory);

            this.server_process.put(this.info.getUuid(), this.p);

            //Register all InputReader for Spigot console
            ConsoleInputReader cir = new ConsoleInputReader(this, Boolean.TRUE);
            ConsoleErrorStreamReader cesr = new ConsoleErrorStreamReader(this);

            this.p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetConfigValues(final File path) {
        UUID server_uuid = info.getUuid();
        int server_port = info.getPort();
        int server_templateid = info.getTemplateID();
        int server_maxplayers = info.getMaxPlayers();
        String server_name = info.getName();

        Properties ps = new Properties();

        try {
            if (!(new File(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\server.properties").exists())) {
                System.out.println("[Server.class] Create server.properties file");
                File server_properties = new File(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\server.properties");
                server_properties.createNewFile();
            }

            System.out.println("[Server.class] Set all server properties for server " + this.info.getName() + "...");

            InputStreamReader inputstreamreader = new InputStreamReader(Files.newInputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\server.properties")));
            ps.load(inputstreamreader);

            //Server Data
            ps.setProperty("online-mode", "false");
            ps.setProperty("server-ip", "");
            ps.setProperty("server-port", Integer.toString(server_port));
            ps.setProperty("max-players", Integer.toString(server_maxplayers));
            ps.setProperty("motd", "\\\u00a7f\\\u00a7lMCONE.EU \\\u00a7r\\\u00bb \\\u00a77" + server_name);

            //World Data
            ps.setProperty("allow-nether", "false");
            ps.setProperty("spawn-animals", "false");
            ps.setProperty("spawn-monsters", "false");
            ps.setProperty("generate-structures", "false");

            //CloudSystem Data
            ps.setProperty("server-uuid", server_uuid.toString());
            ps.setProperty("server-templateID", Integer.toString(server_templateid));
            ps.setProperty("server-name", server_name);

            OutputStream outputstream = Files.newOutputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\server.properties"));
            ps.store(outputstream, "MCONE_WRAPPER");

            System.out.println("[Server.class] Done all server.properties have been set...");
            this.sendResult("[Server." + server_name + "] Done all server.properties have been set...", ServerResultPacket.Result.SUCCESSFUL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CreateConsoleLogDirectory(final File path){
        UUID server_uuid = info.getUuid();
        int server_port = info.getPort();
        int server_templateid = info.getTemplateID();
        int server_maxplayers = info.getMaxPlayers();
        String server_name = info.getName();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String current_date = dateFormat.format(date).replace(":", ".");
        this.current_date = current_date;

        File mc1cloud_directory = new File(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\mc1cloud");
        File errorlog = new File(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\mc1cloud\\Console-" + this.current_date + ".yml");

        OS os = new OS();
        Properties ps = new Properties();

        try{
            System.out.println("[Server.class] Creating ConsoleLog directory and file for server " + server_name + "...");

            if(errorlog.exists() || !(errorlog.exists()) ){
                if(!(mc1cloud_directory.exists())) {
                    mc1cloud_directory.mkdir();
                }

                errorlog.createNewFile();
            }

            System.out.println("[Server.class] Set all ConfigLog values for server " + server_name + "...");

            ps.setProperty("system-os", os.getOs_name());
            ps.setProperty("server-name", server_name);
            ps.setProperty("server-uuid", server_uuid.toString());
            ps.setProperty("server-templateid", Integer.toString(server_templateid));
            ps.setProperty("server_port", Integer.toString(server_port));
            ps.setProperty("server-maxplayers", Integer.toString(server_maxplayers));

            OutputStream outputstream = Files.newOutputStream(Paths.get(Var.getSystemPath() + "\\wrapper\\servers\\" + this.info.getName() + "\\mc1cloud\\Console-" + this.current_date + ".yml"));
            ps.store(outputstream, "MCONE_WRAPPER");

            System.out.println("[Server.class] Done all ConsoleLog values have been set for server " + server_name + "...");
            this.sendResult("[Server." + server_name + "] Done all ConsoleLog values have been set...", ServerResultPacket.Result.SUCCESSFUL);
            return;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendcommand(String command) {
        try {
            if (server_process.containsKey(this.info.getUuid())) {
                if (this.p.isAlive() == Boolean.TRUE) {
                    BufferedWriter input = new BufferedWriter(new OutputStreamWriter(this.p.getOutputStream()));
                    input.write(command);
                    System.out.println("[Server.class] Sent command '" + command + "' to the server " + this.info.getName());
                    this.sendResult("[Server." + this.info.getName() + "] Sent command '" + command + "' to server...", ServerResultPacket.Result.COMMAND);
                } else {
                    System.out.println("[Server.class] The command '" + command + "' could not be sent to Serevr '" + this.info.getName() + "' because the process is dead...");
                    this.sendResult("[Server." + this.info.getName() + "] The command '" + command + "' could not be sent to server '" + this.info.getName() + "' because the process is dead...", ServerResultPacket.Result.COOMMAND_ERROR);
                    return;
                }
            } else {
                System.out.println("[Serevr.class] The command '" + command + "' could not be sent to Server '" + this.info.getName() + "' because it has no process...");
                this.sendResult("[Server." + this.info.getName() + "] The command '" + command + "' could not be sent to server because it has no process...", ServerResultPacket.Result.COOMMAND_ERROR);
                return;
            }
        } catch (IOException e) {
            System.out.println("[Server.class] Error the command '" + command + "' could not be sent to the server " + this.info.getName());
            this.sendResult("[Server." + this.info.getName() + "] the command '" + command + "' cloud not be sent to server...", ServerResultPacket.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void sendResult(String message, ServerResultPacket.Result result) {
        try {
            WrapperServer.getInstance().getChannel().writeAndFlush(new ServerResultPacket("Server.class", message, result));
            System.out.println("[Server.class] The result '" + message.toString() + "\\" + result.toString() + "' was sent to the master...");
        } catch (Exception e) {
            System.out.println("[Server.class] The result could not be sent to the master...");
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (server_process.containsKey(this.info.getUuid())) {
                if (this.p.isAlive() == Boolean.TRUE) {
                    System.out.println("[Server.class] Stopping the server " + this.info.getName() + "...");
                    this.sendcommand("stop");
                    this.info.setState(ServerState.STOPPED);
                    this.sendResult("[Server." + this.info.getName() + "] the server was stopped...", ServerResultPacket.Result.SUCCESSFUL);
                } else {
                    System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be stopped because the process is dead...");
                    this.sendResult("[Server." + this.info.getName() + "] The server cloud not be stopped because the process is dead...", ServerResultPacket.Result.COOMMAND_ERROR);
                    return;
                }
            } else {
                System.out.println("[Serevr.class] The server '" + this.info.getName() + "' could not be stopped because it has no process...");
                this.sendResult("[Server." + this.info.getName() + "] The server could not be stopped because it has no process...", ServerResultPacket.Result.COOMMAND_ERROR);
                return;
            }
        } catch (Exception e) {
            System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be stopped...");
            this.sendResult("[Server." + this.info.getName() + "] The server cloud not be stopped...", ServerResultPacket.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void forceStop() {
        try {
            if (server_process.containsKey(this.info.getUuid())) {
                if (this.p.isAlive() == Boolean.TRUE) {
                    System.out.println("[Server.class] ForceStop server " + this.info.getName() + "...");
                    this.server_process.remove(this.info.getUuid());
                    this.p.destroy();
                    this.info.setState(ServerState.STOPPED);
                    this.sendResult("[Server." + this.info.getName() + "] The server was ForceStopped...", ServerResultPacket.Result.SUCCESSFUL);
                } else {
                    System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be ForeStopped because the process is dead...");
                    this.sendResult("[Server." + this.info.getName() + "] The server cloud not be ForeStopped because the process is dead...", ServerResultPacket.Result.COOMMAND_ERROR);
                    return;
                }
            } else {
                System.out.println("[Serevr.class] The server '" + this.info.getName() + "' could not be ForeStopped because it has no process...");
                this.sendResult("[Server." + this.info.getName() + "] The server could not be ForeStopped because it has no process...", ServerResultPacket.Result.COOMMAND_ERROR);
                return;
            }
        } catch (Exception e) {
            System.out.println("[Server.class] The server '" + this.info.getName() + "' could not be ForeStopped...");
            this.sendResult("[Server." + this.info.getName() + "] The server '" + this.info.getName() + "' could not be ForeStopped...", ServerResultPacket.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void restart() {
        this.info.setState(ServerState.STOPPED);
        /* ... */
        this.info.setState(ServerState.STARTING);
        /* ... */
        this.info.setState(ServerState.RUNNING);
    }

    public void delete() {
        System.out.println("[Server.class] Deleting server " + this.info.getName() + "...");
        if (this.p.isAlive() == Boolean.TRUE) {
            String server_name = this.info.getName();

            this.forceStop();
            WrapperServer.getInstance().getServers().remove(this);

            System.out.println("[Server.class] The server " + server_name + " was deleted...");
            this.sendResult("[Server." + server_name + "] The server was deleted...", ServerResultPacket.Result.SUCCESSFUL);
            return;
        } else {
            String server_name = this.info.getName();

            this.server_process.remove(this.info.getUuid());
            WrapperServer.getInstance().getServers().remove(this);

            System.out.println("[Server.class] The server " + server_name + " was deleted...");
            this.sendResult("[Server." + server_name + "] The server was deleted...", ServerResultPacket.Result.SUCCESSFUL);
            return;
        }
    }

    private String isAlive() {
        String msg = null;

        if (this.p.isAlive() == Boolean.TRUE) {
            msg = "Process " + this.p.toString() + " is Alive";
            return msg;
        } else {
            msg = "Process " + this.p.toString() + " is not Alive";
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
