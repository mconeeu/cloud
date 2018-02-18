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

import java.io.*;

public abstract class Server {

    @Getter
    @Setter
    protected ServerInfo info;
    @Getter
    protected Runtime runtime;
    @Getter
    protected Process process;

    public Server(ServerInfo info, int port) {
        this.info = info;
        this.info.setPort(port);
        WrapperServer.getInstance().getServers().add(this);
    }

    public abstract void start();

    public abstract void stop();

    public void restart() {
        this.info.setState(ServerState.OFFLINE);
        stop();
        this.info.setState(ServerState.STARTING);
        start();
        this.info.setState(ServerState.WAITING);
    }

    public void forcestop() {
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

    public void delete() {
        System.out.println("[Server.class] Deleting server " + this.info.getName() + "...");
        if (process.isAlive() == Boolean.TRUE) {
            String server_name = this.info.getName();

            this.forcestop();
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

    private boolean isAlive() {
        return process.isAlive();
    }

    public void sendCommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    OutputStreamWriter os = new OutputStreamWriter(process.getOutputStream());
                    process.getOutputStream().write((command + "\n").getBytes());
                    process.getOutputStream().flush();

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

    public void sendProgressState(ServerProgressStatePacketMaster.Progress progress) {
        try {
            WrapperServer.getInstance().send(new ServerProgressStatePacketMaster("Server.class", progress));
            System.out.println("[Server.class] Send new progress state '" + progress.toString() + "' to server Master...");
        } catch (Exception e) {
            System.out.println("[Server.clas] Could not be sent new progress state to server Master");
            e.printStackTrace();
        }
    }

}
