/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.directorymanager.CopyManager;
import eu.mcone.cloud.core.network.packet.ServerResultPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.util.Var;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

public class Server {

    @Getter @Setter
    private ServerInfo info;

    @Getter
    private ProcessBuilder pb;

    @Getter
    private Process p;

    @Getter
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
        int port = info.getPort();
        int ram = info.getRam();

        try {
            final String home_directory = Var.home_directory;

            final File server_directory = new File(home_directory + "\\Desktop\\servers\\" + server_name);
            final File target = new File(home_directory + "\\Desktop\\template\\" + server_name);
            final File copy_to = new File(home_directory + "\\Desktop\\servers\\" + server_name + "\\");

            /* Linux directory
            final File server_directory = new File(home_directory + "\\servers\\" + servername);
            final File target = new File(home_directory + "\\temp\\" + servername);
            final File copy_to = new File(home_directory + "\\servers\\" + servername + "\\");
            */

            System.out.println("[WRAPPER] Starting server: " + server_name + ", ram " + ram + " gb, on the port " + port + " ...");

            //Kopiere alle Datein aus dem Verzeichniss in denn target Ordner.
            System.out.println("[WRAPPER] Copy all files to the directory...");
            CopyManager.copyFilesInDirectory(target, copy_to);

            ProcessBuilder pb = new ProcessBuilder(
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
                    "-Xmx"+info.getRam()+"M",
                    server_directory + "\\spigot.jar");

            pb.redirectErrorStream(true);

            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            //pb.redirectInput();

            pb.directory(server_directory);

            java.lang.Process p = pb.start();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            writer.flush();

            this.SetConfigValues(server_directory);

            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetConfigValues(File path) {
        this.sendResult("Start spigot server", ServerResultPacket.Result.INFORMATION);
        UUID server_uuid = info.getUuid();
        int server_port = info.getPort();
        int server_templateid = info.getTemplateID();
        int server_maxplayers = info.getMaxPlayers();
        String server_name = info.getName();

        Properties ps = new Properties();

        try {
            if (!(new File(path + "\\server.properties").exists())) {
                System.out.println("[WRAPPER] Create server.properties file");
                File server_properties = new File(path + "\\server.properties");
                server_properties.createNewFile();
            }

            System.out.println("[WRAPPER] Set server properties");

            InputStreamReader inputstreamreader = new InputStreamReader(Files.newInputStream(Paths.get(path + "\\server.properties")));
            ps.load(inputstreamreader);

            //Server Data
            ps.setProperty("online-mode", "false");
            ps.setProperty("server-ip", "");
            ps.setProperty("server-port", Integer.toString(server_port));
            ps.setProperty("max-players", Integer.toString(server_maxplayers));
            ps.setProperty("motd", "\\u00a7f\\u00a7lMCONE.EU \\u00a7r\\u00bb \\u00a77" + server_name);

            //World Data
            ps.setProperty("allow-nether", "false");
            ps.setProperty("spawn-animals", "false");
            ps.setProperty("spawn-monsters", "false");
            ps.setProperty("generate-structures", "false");

            //CloudSystem Data
            ps.setProperty("server-uuid", server_uuid.toString());
            ps.setProperty("server-templateID", Integer.toString(server_templateid));
            ps.setProperty("server-name", server_name);

            OutputStream outputstream = Files.newOutputStream(Paths.get(path + "\\server.properties"));
            ps.store(outputstream, "MCONE_WRAPPER");

            System.out.println("[WRAPPER] Set all server properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendcommand(String command) {
        this.sendResult("Send command Method in class Server", ServerResultPacket.Result.INFORMATION);

        /*
        try (BufferedWriter input = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
            input.write(command);
            System.out.println("[WRAPPER] Send command '" + command + "' to server " + this.info.getName());
        } catch (IOException e) {
            System.out.println("[WRAPPER] Error in method SendCommand");
            e.printStackTrace();
        }
        */
    }

    private void sendResult(String message, ServerResultPacket.Result result){
        try{
            WrapperServer.getInstance().getChannel().writeAndFlush(new ServerResultPacket("Server.class", message, result));
            System.out.println("[WRAPPER] Send ServerResultPacket to master");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            System.out.println("[WRAPPER] Send command 'Stop' to server " + this.info.getName());
            this.info.setState(ServerState.STOPPED);
        } catch (Exception e) {
            System.out.println("[WRAPPER] Error in method StopServer");
            e.printStackTrace();
        }
    }

    public void forceStop() {
        try {
            if (p.isAlive() == Boolean.TRUE) {
                System.out.println("[WRAPPER] Forcestop server " + this.info.getName());
                p.destroy();
                this.info.setState(ServerState.STOPPED);
            } else {
                return;
            }
        } catch (Exception e) {
            System.out.println("[WRAPPER] Error in method ForceStop");
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
        this.forceStop();
        WrapperServer.getInstance().getServers().remove(this);
    }

    public String isAlive() {
        String msg = null;

        if (p.isAlive()) {
            msg = "Process " + pb.inheritIO() + " is Alive";
            return msg;
        } else {
            msg = "Process " + pb.inheritIO() + " is not Alive";
            return msg;
        }
    }

    private int calculatePort() {
        int port = 4000;
        while (WrapperServer.getInstance().getServers().iterator().hasNext()) port = WrapperServer.getInstance().getServers().iterator().next().getInfo().getPort();

        port++;
        return port;
    }

}
