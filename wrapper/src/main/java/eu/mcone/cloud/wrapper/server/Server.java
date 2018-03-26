/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.file.UnZip;
import eu.mcone.cloud.core.network.packet.ServerResultPacketWrapper;
import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.jenkins.JenkinsDownloader;
import eu.mcone.cloud.wrapper.server.console.ConsoleInputReader;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;

public abstract class Server {

    private static final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();

    @Getter @Setter
    protected ServerInfo info;
    @Getter
    protected Runtime runtime;
    @Getter
    protected Process process;
    @Getter
    protected File serverDir;
    @Getter
    protected ServerState state = ServerState.OFFLINE;
    @Getter
    private ConsoleInputReader reader;

    public Server(ServerInfo info) {
        this.info = info;

        if (info.isStaticServer()) {
            this.serverDir = new File(homeDir + File.separator + "staticservers" + File.separator + info.getName());
        } else {
            this.serverDir = new File(homeDir + File.separator + "servers" + File.separator + info.getName());
        }

        WrapperServer.getInstance().getServers().add(this);
    }

    public abstract void start();

    public abstract void stop();

    abstract void setConfig() throws IOException;

    void initialise(final int port, final Class<? extends ConsoleInputReader> reader, final String[] command) {
        info.setPort(port);

        Logger.log(getClass(), "["+info.getName()+"] Starting server (Version: '"+info.getVersion()+"', UUID: '" + info.getUuid() + "', Template: '" + info.getTemplateName() + "', RAM: '" + info.getRam() + "M, Port: '" + info.getPort() + "')...");
        setState(ServerState.STARTING);

        WrapperServer.getInstance().getThreadPool().execute(() -> {
            final File templateZip = new File(serverDir+File.separator+info.getTemplateName()+".zip");
            final File executable = new File(homeDir+File.separator+"jars"+File.separator+info.getVersion().toString()+".jar");

            try {
                if (!info.isStaticServer()) {
                    if (serverDir.exists()) FileUtils.deleteDirectory(serverDir);
                    serverDir.mkdir();

                    Logger.log(getClass(), "[" + info.getName() + "] Downloading Template...");
                    URL website = new URL("http://templates.mcone.eu/" + info.getTemplateName() + ".zip");
                    FileOutputStream fos = new FileOutputStream(templateZip);
                    fos.getChannel().transferFrom(Channels.newChannel(website.openStream()), 0, Long.MAX_VALUE);

                    Logger.log(getClass(), "[" + info.getName() + "] Unzipping Template...");
                    new UnZip(templateZip.getPath(), serverDir.getPath());
                    templateZip.delete();
                } else {
                    if (!serverDir.exists()) serverDir.mkdir();
                }

                Logger.log(getClass(), "Implementing Jenkins Files");
                FileUtils.copyFile(
                        new JenkinsDownloader(JenkinsDownloader.CiServer.MCONE).getJenkinsArtifact("MCONE-Cloud", "plugin"),
                        new File(serverDir + File.separator + "plugins" + File.separator + "MCONE-CloudPlugin.jar")
                );

                FileUtils.copyFile(executable, new File(serverDir+File.separator+"server.jar"));

                setConfig();

                this.runtime = Runtime.getRuntime();
                this.process = this.runtime.exec(command, null, serverDir);

                //Register all Output for Server console
                this.reader = reader.getDeclaredConstructor(Server.class, Boolean.class).newInstance(this, true);

                this.process.waitFor();
                this.process.destroy();
                Logger.log(getClass(), "["+info.getName()+"] Server stopped!");

                if (state.equals(ServerState.WAITING)) {
                    Logger.log(getClass(), "["+info.getName()+"] Server seems to be crashed! Restarting...");
                    start();
                } else if (state.equals(ServerState.STARTING)) {
                    Logger.err(getClass(), "["+info.getName()+"] Server crashed while starting! Fix this problem before starting it again!");
                }
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                Logger.log(getClass(), "["+info.getName()+"] Could not start server:");
                if (e instanceof FileNotFoundException) {
                    Logger.err(getClass(), "["+info.getName()+"] Template does not exist, cancelling...");
                    e.printStackTrace();
                    return;
                }
                e.printStackTrace();
            }
        });
        Logger.log(getClass(), "["+info.getName()+"] Server start initialised, method returned");
    }

    public void restart() {
        stop();
        start();
    }

    public void forcestop() {
        if (process != null) {
            if (process.isAlive()) {
                Logger.log(getClass(), "["+info.getName()+"] ForceStop server " + info.getName() + "...");
                process.destroy();
                setState(ServerState.OFFLINE);
                sendResult("[Server." + this.info.getName() + "] The server was ForceStopped...", ServerResultPacketWrapper.Result.SUCCESSFUL);
            } else {
                Logger.log(getClass(), "["+info.getName()+"] Could not be forcestop server because the process is dead...");
                this.sendResult("[Server." + this.info.getName() + "] The server cloud not be ForeStopped because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } else {
            Logger.log(getClass(), "["+info.getName()+"] Could not forcestop because server has no process...");
            this.sendResult("[Server." + this.info.getName() + "] The server could not be ForeStopped because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
        }
    }

    public void delete() {
        Logger.log(getClass(), "["+info.getName()+"] Deleting server...");
        if (process.isAlive()) this.forcestop();

        String server_name = this.info.getName();
        WrapperServer.getInstance().getServers().remove(this);

        Logger.log(getClass(), "["+info.getName()+"] Server deleted...");
        this.sendResult("[Server." + server_name + "] The server was deleted...", ServerResultPacketWrapper.Result.SUCCESSFUL);
    }

    private boolean isAlive() {
        return process.isAlive();
    }

    public void sendCommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    out.write(command+"\n");
                    out.flush();

                    Logger.log(getClass(), "["+info.getName()+"] Sent command '" + command + "'");
                    this.sendResult("[Server." + this.info.getName() + "] Sent command '" + command + "' to server...", ServerResultPacketWrapper.Result.COMMAND);
                } else {
                    Logger.err(getClass(), "["+info.getName()+"] Could not send command '" + command + "' because the process is dead...");
                    this.sendResult("[Server." + this.info.getName() + "] The command '" + command + "' could not be sent to server '" + this.info.getName() + "' because the process is dead...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
                }
            } else {
                Logger.err(getClass(), "["+info.getName()+"] Could not send command '" + command + "' because server has no process...");
                this.sendResult("[Server." + this.info.getName() + "] The command '" + command + "' could not be sent to server because it has no process...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            }
        } catch (IOException e) {
            Logger.err(getClass(), "["+info.getName()+"] Could not send command '" + command + "' to the server (IOException)");
            this.sendResult("[Server." + this.info.getName() + "] the command '" + command + "' cloud not be sent to server...", ServerResultPacketWrapper.Result.COOMMAND_ERROR);
            e.printStackTrace();
        }
    }

    public void setState(ServerState state) {
        this.state = state;
        WrapperServer.getInstance().send(new ServerUpdateStatePacket(info.getUuid(), state));
    }

    void sendResult(String message, ServerResultPacketWrapper.Result result) {
        WrapperServer.getInstance().send(new ServerResultPacketWrapper("Server.class", message, result));
        System.out.println("[Server.class] The result '" + message + "\\" + result.toString() + "' was sent to the master...");
    }

    private static int getNextAvailablePort(int port) {
        boolean result;
        Socket s = null;

        try {
            s = new Socket("localhost", port);
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            if(s != null) try {s.close();} catch(Exception ignored){}
        }

        if (result) {
            return port;
        } else {
            return getNextAvailablePort(++port);
        }
    }

}
