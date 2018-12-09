/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin;

import eu.mcone.cloud.api.plugin.CloudAPI;
import eu.mcone.cloud.core.packet.ServerListUpdatePacketPlugin;
import eu.mcone.cloud.core.packet.ServerRegisterPacketPlugin;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.plugin.handler.ServerListUpdateHandler;
import eu.mcone.networkmanager.api.network.client.ClientBootstrap;
import eu.mcone.networkmanager.api.network.client.NetworkmanagerClient;
import eu.mcone.networkmanager.api.network.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private String serverName, hostname;
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
            FileInputStream fis = new FileInputStream(new File("server.properties"));
            ps.load(fis);

            serverName = ps.getProperty("server-name");
            serverUuid = UUID.fromString(ps.getProperty("server-uuid"));
            hostname = ps.getProperty("wrapper-ip");
            port = Integer.valueOf(ps.getProperty("server-port"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        nettyBootstrap = new ClientBootstrap("localhost", "", new NetworkmanagerClient() {
            @Override
            public void runAsync(Runnable runnable) {
                plugin.runAsync(runnable);
            }
            @Override
            public void onChannelActive(ChannelHandlerContext chc) {
                channel = chc.channel();
                ServerListUpdateHandler.setNewConnection(true);

                chc.writeAndFlush(new ServerRegisterPacketPlugin(serverUuid, hostname, port, plugin.getPlayerCount(), serverState));
                System.out.println("new channel to " + chc.channel().remoteAddress().toString());
            }
            @Override
            public void onChannelUnregistered(ChannelHandlerContext channelHandlerContext) {}
        });
        nettyBootstrap.getPacketManager().registerPacketHandler(ServerListUpdatePacketPlugin.class, new ServerListUpdateHandler());
    }

    public void unload() {
        channel.close();
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

}
