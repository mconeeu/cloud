/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.exception.CloudException;
import eu.mcone.cloud.core.file.UnZip;
import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.world.CloudWorld;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.download.JenkinsDownloader;
import eu.mcone.cloud.wrapper.download.WorldDownloader;
import eu.mcone.cloud.wrapper.server.console.ConsoleInputReader;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

public abstract class Server {

    private static final File homeDir = WrapperServer.getInstance().getFileManager().getHomeDir();
    protected Gson gson;

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
    protected ServerProperties properties;
    @Getter
    private ConsoleInputReader reader;

    public Server(ServerInfo info) {
        this.gson = new Gson();
        this.info = info;
        this.properties = gson.fromJson(info.getProperties(), ServerProperties.class);

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
        if (!state.equals(ServerState.OFFLINE)) {
            Logger.err(getClass(), "["+info.getName()+"] Cannot start Server if not OFFLINE");
            return;
        }

        info.setPort(port);

        Logger.log(getClass(), "["+info.getName()+"] Starting server (Version: '"+info.getVersion()+"', UUID: '" + info.getUuid() + "', Template: '" + info.getTemplateName() + "', RAM: '" + info.getRam() + "M, Port: '" + info.getPort() + "')...");
        setState(ServerState.STARTING);

        WrapperServer.getInstance().getThreadPool().execute(() -> {
            final File executable = new File(homeDir+File.separator+"jars"+File.separator+info.getVersion().toString()+".jar");
            final File worldFile = new File(serverDir+File.separator+"worlds.json");

            try {
                if (!info.isStaticServer()) {
                    if (serverDir.exists()) FileUtils.deleteDirectory(serverDir);
                    serverDir.mkdir();

                    for (ServerProperties.PluginDownload download : properties.getPlugins()) {
                        File plugin = new JenkinsDownloader(JenkinsDownloader.CiServer.valueOf(download.getCiServer())).getJenkinsArtifact(download.getJob(), download.getArtifact());

                        Logger.log(getClass(), "["+info.getName()+"] Implementing Plugin "+download.getJob()+":"+download.getArtifact());
                        FileUtils.copyFile(
                                plugin,
                                new File(serverDir + File.separator + "plugins" + File.separator + plugin.getName())
                        );
                    }

                    JsonArray worldArray = new JsonArray();
                    for (String w : properties.getWorlds()) {
                        CloudWorld world = new WorldDownloader(w).downloadWorld();
                        worldArray.add(gson.toJsonTree(world, CloudWorld.class));

                        Logger.log(getClass(), "["+info.getName()+"] Implementing World "+w);
                        new UnZip(
                                world.getFilePath(),
                                serverDir.getPath() + File.separator + w
                        );
                    }

                    worldFile.createNewFile();
                    FileUtils.writeStringToFile(worldFile, gson.toJson(worldArray));
                } else {
                    if (!serverDir.exists()) serverDir.mkdir();
                }

                Logger.log(getClass(), "Implementing Cloud-Plugin");
                FileUtils.copyFile(
                        //new JenkinsDownloader(JenkinsDownloader.CiServer.MCONE).getJenkinsArtifact("MCONE-Cloud", "plugin"),
                        new File("D:\\Rufus Maiwald\\Documents\\Java\\Projekte\\mc1cloud\\out\\artifacts\\plugin_jar\\plugin.jar"),
                        new File(serverDir + File.separator + "plugins" + File.separator + "MCONE-CloudPlugin.jar")
                );

                FileUtils.copyFile(executable, new File(serverDir+File.separator+"server.jar"));

                setConfig();
                for (ServerProperties.Config config : properties.getConfigs()) {
                    new ConfigSetter(this, config);
                }

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
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | CloudException e) {
                Logger.log(getClass(), "["+info.getName()+"] Could not start server:");
                if (e instanceof CloudException) {
                    Logger.err(getClass(), "["+info.getName()+"] "+e.getMessage());
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
            } else {
                Logger.log(getClass(), "["+info.getName()+"] Could not be forcestop server because the process is dead...");
            }
        } else {
            Logger.log(getClass(), "["+info.getName()+"] Could not forcestop because server has no process...");
        }
    }

    public void delete() {
        Logger.log(getClass(), "["+info.getName()+"] Deleting server...");
        if (process.isAlive()) this.forcestop();

        String server_name = this.info.getName();
        WrapperServer.getInstance().getServers().remove(this);

        Logger.log(getClass(), "["+info.getName()+"] Server deleted...");
    }

    public void sendCommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    out.write(command+"\n");
                    out.flush();

                    Logger.log(getClass(), "["+info.getName()+"] Sent command '" + command + "'");
                } else {
                    Logger.err(getClass(), "["+info.getName()+"] Could not send command '" + command + "' because the process is dead...");
                }
            } else {
                Logger.err(getClass(), "["+info.getName()+"] Could not send command '" + command + "' because server has no process...");
            }
        } catch (IOException e) {
            Logger.err(getClass(), "["+info.getName()+"] Could not send command '" + command + "' to the server (IOException)");
            e.printStackTrace();
        }
    }

    public void setState(ServerState state) {
        this.state = state;
        WrapperServer.getInstance().send(new ServerUpdateStatePacket(info.getUuid(), state));
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
