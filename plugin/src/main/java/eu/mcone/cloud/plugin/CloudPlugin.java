/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin;

import eu.mcone.cloud.api.plugin.CloudAPI;
import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.plugin.network.ClientBootstrap;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class CloudPlugin extends CloudAPI {

    @Getter
    private eu.mcone.cloud.api.plugin.CloudPlugin plugin;
    @Getter
    private ClientBootstrap nettyBootstrap;
    @Getter @Setter
    private Channel channel;
    @Getter
    private String name, hostname;
    @Getter @Setter
    private ServerState serverState = ServerState.WAITING;
    @Getter
    private List<CloudWorld> loadedWorlds;
    @Getter
    private UUID serverUuid;
    @Getter
    private int port;

    public CloudPlugin(eu.mcone.cloud.api.plugin.CloudPlugin plugin) {
        setInstance(this);
        this.plugin = plugin;
        this.loadedWorlds = new ArrayList<>();

        try {
            Properties ps = new Properties();
            ps.load(new InputStreamReader(Files.newInputStream(Paths.get("server.properties"))));

            name = ps.getProperty("server-name");
            serverUuid = UUID.fromString(ps.getProperty("server-uuid"));
            hostname = ps.getProperty("wrapper-ip");
            port = Integer.valueOf(ps.getProperty("server-port"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        nettyBootstrap = new ClientBootstrap("localhost", 4567, this);
    }

    public void unload() {
        channel.close();
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

}
