/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.exception.CloudException;
import eu.mcone.cloud.core.file.UnZip;
import eu.mcone.cloud.core.network.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.download.CiServer;
import eu.mcone.cloud.wrapper.download.JenkinsDownloader;
import eu.mcone.cloud.wrapper.download.WorldDownloader;
import eu.mcone.cloud.wrapper.server.console.ConsoleInputReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

@Log
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
    protected ServerProperties properties;
    @Getter
    private ConsoleInputReader reader;

    public Server(ServerInfo info) {
        this.info = info;
        this.properties = WrapperServer.getInstance().getGson().fromJson(info.getProperties(), ServerProperties.class);

        if (info.isStaticServer()) {
            this.serverDir = new File(homeDir + File.separator + "staticservers" + File.separator + info.getName());
        } else {
            this.serverDir = new File(homeDir + File.separator + "servers" + File.separator + info.getName());
        }
        System.out.println();
        WrapperServer.getInstance().getServers().add(this);
    }

    public abstract void start();

    public abstract void stop();

    abstract void setConfig() throws IOException;

    void initialise(final int port, final Class<? extends ConsoleInputReader> reader, final String[] command) {
        if (!state.equals(ServerState.OFFLINE)) {
            log.severe("[" + info.getName() + "] Cannot start Server if not OFFLINE");
            return;
        }

        info.setPort(port);

        log.info("[" + info.getName() + "] Starting server (Version: '" + info.getVersion() + "', UUID: '" + info.getUuid() + "', Template: '" + info.getTemplateName() + "', RAM: '" + info.getRam() + "M, Port: '" + info.getPort() + "')...");
        setState(ServerState.STARTING);

        WrapperServer.getInstance().getThreadPool().execute(() -> {
            final File executable = new File(homeDir + File.separator + "jars" + File.separator + info.getVersion().toString() + ".jar");

            try {
                if (!info.isStaticServer()) {
                    if (serverDir.exists()) FileUtils.deleteDirectory(serverDir);
                    serverDir.mkdir();

                    for (ServerProperties.PluginDownload download : properties.getPlugins()) {
                        File plugin = new JenkinsDownloader(CiServer.valueOf(download.getCiServer())).getJenkinsArtifact(download.getJob(), download.getArtifact());

                        if (plugin != null) {
                            log.info("[" + info.getName() + "] Implementing Plugin " + download.getJob() + ":" + download.getArtifact());
                            FileUtils.copyFile(
                                    plugin,
                                    new File(serverDir + File.separator + "plugins" + File.separator + plugin.getName())
                            );
                        } else {
                            log.info("[" + info.getName() + "] Plugin " + download.getJob() + ":" + download.getArtifact() + " could not be found. Aborting download...");
                        }
                    }

                    dowloadWorlds();
                } else {
                    if (!serverDir.exists()) serverDir.mkdir();
                }

                log.info("Implementing Cloud-Plugin");
                FileUtils.copyFile(
                        new JenkinsDownloader(CiServer.MCONE).getJenkinsArtifact("MCONE-Cloud", "mcone-cloud-plugin"),
                        new File(serverDir + File.separator + "plugins" + File.separator + "MCONE-CloudPlugin.jar")
                );

                FileUtils.copyFile(executable, new File(serverDir + File.separator + "server.jar"));

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
                log.info("[" + info.getName() + "] Server stopped!");

                if (state.equals(ServerState.WAITING)) {
                    log.info("[" + info.getName() + "] Server seems to be crashed! Restarting...");
                    state = ServerState.OFFLINE;
                    start();
                } else if (state.equals(ServerState.STARTING)) {
                    log.info("[" + info.getName() + "] Server crashed while starting! Fix this problem before starting it again!");
                }
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | CloudException e) {
                log.info("[" + info.getName() + "] Could not start server:");
                if (e instanceof CloudException) {
                    log.severe("[" + info.getName() + "] " + e.getMessage());
                    e.printStackTrace();
                    return;
                }

                e.printStackTrace();
            }
        });
        log.info("[" + info.getName() + "] Server start initialised, method returned");
    }

    public void restart() {
        stop();
        start();
    }

    public void forcestop() {
        if (process != null) {
            if (process.isAlive()) {
                log.info("[" + info.getName() + "] ForceStop server " + info.getName() + "...");
                process.destroy();
                setState(ServerState.OFFLINE);
            } else {
                log.info("[" + info.getName() + "] Could not be forcestop server because the process is dead...");
            }
        } else {
            log.info("[" + info.getName() + "] Could not forcestop because server has no process...");
        }
    }

    public void delete() {
        log.info("[" + info.getName() + "] Deleting server...");
        if (process.isAlive()) this.forcestop();

        WrapperServer.getInstance().getServers().remove(this);
        log.info("[" + info.getName() + "] Server deleted...");
    }

    public void sendCommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    out.write(command + "\n");
                    out.flush();

                    log.info("[" + info.getName() + "] Sent command '" + command + "'");
                } else {
                    log.severe("[" + info.getName() + "] Could not send command '" + command + "' because the process is dead...");
                }
            } else {
                log.severe("[" + info.getName() + "] Could not send command '" + command + "' because server has no process...");
            }
        } catch (IOException e) {
            log.severe("[" + info.getName() + "] Could not send command '" + command + "' to the server (IOException)");
            e.printStackTrace();
        }
    }

    private void dowloadWorlds() throws IOException {
        for (String w : properties.getWorlds()) {
            CloudWorld world = new WorldDownloader(w).download();
            log.info("[" + info.getName() + "] Implementing World " + w);
            new UnZip(world.getFilePath(), serverDir.getPath() + File.separator + w);
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
            if (s != null) try {
                s.close();
            } catch (Exception ignored) {
            }
        }

        if (result) {
            return port;
        } else {
            return getNextAvailablePort(++port);
        }
    }

}
