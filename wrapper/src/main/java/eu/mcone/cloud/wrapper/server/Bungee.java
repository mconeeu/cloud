/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Bungee {

    @Getter
    @Setter
    private ServerInfo info;
    @Getter
    private ProcessBuilder processBuilder;
    @Getter
    private Process process;

    public Bungee(ServerInfo info) {
        this.info = info;
        this.info.setPort(calculatePort());
        WrapperServer.getInstance().getBungees().add(this);
    }


    public void start() {
        final String s = File.separator;
        final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
        final File serverDir = new File(homeDir + s + "wrapper" + s + "bungee" + s);

        System.out.println("[Bungee.class] Starting new Bungeecord with the UUID: '" + info.getUuid() + "', '" + info.getRam() + "gb ram, on the port '" + info.getPort() + "'...");

        if (serverDir.exists()) serverDir.delete();
        serverDir.mkdir();


        new Thread(() -> {
            try{
                processBuilder = new ProcessBuilder().inheritIO()
                        .command("java",
                                "-Dfile.encoding=UTF-8",
                                "-jar",
                                "-XX:+UseG1GC",
                                "-XX:MaxGCPauseMillis=50",
                                "-XX:-UseAdaptiveSizePolicy",
                                "-Dio.netty.recycler.maxCapacity=0 ",
                                "-Dio.netty.recycler.maxCapacity.default=0",
                                "-Djline.terminal=jline.UnsupportedTerminal",
                                "-Xmx" + info.getRam() + "M",
                                serverDir + s + "bungee.jar"
                        ).redirectError(ProcessBuilder.Redirect.PIPE)
                        .redirectInput(ProcessBuilder.Redirect.PIPE)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE);

                this.process = this.processBuilder.start();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.flush();

                new ConsoleInputReaderBungee(this, true);

                this.process.waitFor();
                this.process.destroy();
            } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            }
        }).start();
        System.out.println("[Bungee.class] Bungeecord start of "+info.getName()+" initialised, method returned");
    }

    public void stop() {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    System.out.println("[Bungee.class] Stopping the server " + this.info.getName() + "...");
                    this.sendcommand("stop");
                    this.info.setState(ServerState.OFFLINE);
                    this.sendResult("[Bungee." + this.info.getName() + "] the server was stopped...", ServerResultPacketWrapper.Result.SUCCESSFUL);
                } else {
                    System.out.println("[Bungee.class] The server '" + this.info.getName() + "' could not be stopped because the process is dead...");
                    this.sendResult("[Bungee." + this.info.getName() + "] The server cloud not be stopped because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
                }
            } else {
                System.out.println("[Bungee.class] The server '" + this.info.getName() + "' could not be stopped because it has no process...");
                this.sendResult("[Bungee." + this.info.getName() + "] The server could not be stopped because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
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
                    System.out.println("[Bungee.class] ForceStop server " + this.info.getName() + "...");
                    process.destroy();
                    info.setState(ServerState.OFFLINE);
                    sendResult("[Bungee.class] The server was ForceStopped...", ServerResultPacketWrapper.Result.SUCCESSFUL);
                } else {
                    System.out.println("[Bungee.class] The server '" + this.info.getName() + "' could not be ForeStopped because the process is dead...");
                    this.sendResult("[Bungee.class] The server cloud not be ForeStopped because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
                }
            } else {
                System.out.println("[Bungee.class] The server '" + this.info.getName() + "' could not be ForeStopped because it has no process...");
                this.sendResult("[Bungee.class] The server could not be ForeStopped because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } catch (Exception e) {
            System.out.println("[Bungee.class] The server '" + this.info.getName() + "' could not be ForeStopped...");
            this.sendResult("[Bungee.class] The server '" + this.info.getName() + "' could not be ForeStopped...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void delete() {
        System.out.println("[Bungee.class] Deleting server " + this.info.getName() + "...");
        if (process.isAlive() == Boolean.TRUE) {
            String server_name = this.info.getName();

            this.forceStop();
            WrapperServer.getInstance().getServers().remove(this);

            System.out.println("[Bungee.class] The server " + server_name + " was deleted...");
            this.sendResult("[Bungee.class] The Bungeecord was deleted...", ServerResultPacketWrapper.Result.SUCCESSFUL);
        } else {
            String server_name = this.info.getName();
            WrapperServer.getInstance().getServers().remove(this);

            System.out.println("[Bungee.class] The server " + server_name + " was deleted...");
            this.sendResult("[Bungee.class] The Bungeecord was deleted...", ServerResultPacketWrapper.Result.SUCCESSFUL);
        }
    }

    public void sendcommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive() == Boolean.TRUE) {
                    BufferedWriter input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    input.write(command);
                    System.out.println("[Bungee.class] Sent command '" + command + "' to the bungeecord " + this.info.getName());
                    this.sendResult("[Bungee." + this.info.getName() + "] Sent command '" + command + "' to bungeecord...", ServerResultPacketWrapper.Result.COMMAND);
                } else {
                    System.out.println("[Bungee.class] The command '" + command + "' could not be sent to bungeecord '" + this.info.getName() + "' because the process is dead...");
                    this.sendResult("[Bungee." + this.info.getName() + "] The command '" + command + "' could not be sent to bungeecord '" + this.info.getName() + "' because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
                }
            } else {
                System.out.println("[Bungee.class] The command '" + command + "' could not be sent to bungeecord '" + this.info.getName() + "' because it has no process...");
                this.sendResult("[Bungee." + this.info.getName() + "] The command '" + command + "' could not be sent to bungeecord because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } catch (IOException e) {
            System.out.println("[Bungee.class] Error the command '" + command + "' could not be sent to the bungeecord " + this.info.getName());
            this.sendResult("[Bungee." + this.info.getName() + "] the command '" + command + "' cloud not be sent to bungeecord...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void sendResult(String message, ServerResultPacketWrapper.Result result) {
        try {
            WrapperServer.getInstance().send(new ServerResultPacketWrapper("Bungee.class", message, result));
            System.out.println("[Bungee.class] The result '" + message + "\\" + result.toString() + "' was sent to the master...");
        } catch (Exception e) {
            System.out.println("[Bungee.class] The result could not be sent to the master...");
            e.printStackTrace();
        }
    }

    private int calculatePort() {
        int port = 2000;

        for (Bungee b : WrapperServer.getInstance().getBungees()) {
            port = b.getInfo().getPort();
        }

        port++;
        return port;
    }

}
