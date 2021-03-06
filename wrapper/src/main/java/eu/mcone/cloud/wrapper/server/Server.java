/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import eu.mcone.cloud.core.file.Downloader;
import eu.mcone.cloud.core.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerProperties;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.core.exception.DownloadException;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Log
public abstract class Server {

    private static final File HOME_DIR = WrapperServer.getInstance().getFileManager().getHomeDir();
    private final static File JAR_DIR = new File(WrapperServer.getInstance().getFileManager().getHomeDir().getPath() + File.separator + "jars" + File.separator + "plugins");

    @Getter
    @Setter
    protected ServerInfo info;
    @Getter
    protected Runtime runtime;
    @Getter
    protected Process process;
    @Getter
    protected File serverDir;
    @Getter
    private final File pluginDir;
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
            this.serverDir = new File(HOME_DIR + File.separator + "staticservers" + File.separator + info.getName());
        } else {
            this.serverDir = new File(HOME_DIR + File.separator + "servers" + File.separator + info.getName());
        }
        pluginDir = new File(serverDir, "plugins");

        WrapperServer.getInstance().getServers().add(this);
    }

    public abstract void doStop();

    public abstract void start();

    public void stop() {
        if (process != null) {
            if (process.isAlive()) {
                log.info("[" + info.getName() + "] Stopping server...");
                doStop();
            } else {
                log.warning("[" + info.getName() + "] Process is already dead!");
            }
        } else {
            log.warning("[" + info.getName() + "] Process is null!");
        }

        setState(ServerState.OFFLINE);
    }

    public void restart() {
        if (process != null) {
            if (process.isAlive()) {
                log.info("[" + info.getName() + "] Stopping server...");
                state = ServerState.OFFLINE;
                doStop();

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                log.warning("[" + info.getName() + "] Could not stop server because the process is dead!");
            }
        } else {
            log.severe("[" + info.getName() + "] Could not stop server because it has no process!");
        }

        start();
    }

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
            final File executable = new File(HOME_DIR + File.separator + "jars" + File.separator + info.getVersion().toString() + ".jar");

            try {
                if (!info.isStaticServer()) {
                    if (serverDir.exists()) FileUtils.deleteDirectory(serverDir);
                    serverDir.mkdir();

                    dowloadWorlds();
                } else {
                    if (!serverDir.exists()) serverDir.mkdir();
                }

                if (!pluginDir.exists()) pluginDir.mkdirs();

                for (String download : properties.getPlugins()) {
                    String[] path = download.split("/");
                    File plugin = new File(JAR_DIR, path[path.length - 1]);
                    Downloader.download(download, plugin);

                    if (plugin.exists()) {
                        log.info("[" + info.getName() + "] Implementing Plugin " + plugin.getName());
                        Files.copy(
                                plugin.toPath(),
                                Paths.get(serverDir.getPath(), "plugins", plugin.getName()),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } else {
                        log.info("[" + info.getName() + "] Artifact " + path[path.length - 1] + " from url " + download + " could not be downloaded!");
                    }
                }

                log.info("Implementing Cloud-Plugin");
                Files.copy(
                        Paths.get(JAR_DIR.getPath(), "plugins", "mcone-cloud-plugin.jar"),
                        Paths.get(serverDir.getPath(), "plugins", "mcone-cloud-plugin.jar"),
                        StandardCopyOption.REPLACE_EXISTING
                );

                Files.copy(executable.toPath(), Paths.get(serverDir.getPath(), "server.jar"), StandardCopyOption.REPLACE_EXISTING);

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
                this.process.destroyForcibly();
                log.info("[" + info.getName() + "] Server stopped!");

                if (state.equals(ServerState.WAITING)) {
                    log.info("[" + info.getName() + "] Server seems to be crashed! Restarting...");
                    state = ServerState.OFFLINE;
                    start();
                } else if (state.equals(ServerState.STARTING)) {
                    log.info("[" + info.getName() + "] Server crashed while starting! Fix this problem before starting it again!");
                }
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | DownloadException e) {
                log.severe("[" + info.getName() + "] Could not start server:");

                if (e instanceof DownloadException) {
                    log.severe("[" + info.getName() + "] Could not download the Cloud-Plugin from Gitlab Server!");
                    e.printStackTrace();
                    return;
                }

                e.printStackTrace();
            }
        });
    }

    public void forcestop() {
        if (process != null) {
            if (process.isAlive()) {
                log.info("[" + info.getName() + "] ForceStop server " + info.getName() + "...");
                process.destroy();
            } else {
                log.info("[" + info.getName() + "] Process is already dead!");
            }
        } else {
            log.warning("[" + info.getName() + "] Process was null!");
        }

        setState(ServerState.OFFLINE);
    }

    public void delete() {
        log.info("[" + info.getName() + "] Deleting server...");

        if (process.isAlive()) this.forcestop();
        WrapperServer.getInstance().getServers().remove(this);
    }

    public void sendCommand(String command) {
        try {
            if (process != null) {
                if (process.isAlive()) {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    out.write(command + "\n");
                    out.flush();
                    out.close();

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

    // TODO implement new cloud world manager
    private void dowloadWorlds() throws IOException {
        for (String w : properties.getWorlds()) {
//            CloudWorld world = new WorldDownloader(w).download();
            log.info("[" + info.getName() + "] Implementing World " + w);
//            new UnZip(world.getFilePath(), serverDir.getPath() + File.separator + w);
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
