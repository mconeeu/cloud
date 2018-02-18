/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.plugin.network.ClientBootstrap;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

public class CloudPlugin {

    @Getter
    private Plugin plugin;
    @Getter @Setter
    private Channel channel;
    @Getter
    private String name, hostname;
    @Getter
    private UUID serverUuid;
    @Getter
    private int port;

    public CloudPlugin(Plugin plugin) {
        this.plugin = plugin;

        Properties ps = new Properties();
        try {
            ps.load(new InputStreamReader(Files.newInputStream(Paths.get("server.properties"))));

            name = ps.getProperty("server-name");
            serverUuid = UUID.fromString(ps.getProperty("server-uuid"));
            hostname = ps.getProperty("server-ip");
            port = Integer.valueOf(ps.getProperty("server-port"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ClientBootstrap("localhost", 4567, this);
    }

    public void unload() {
        channel.close();
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

}
