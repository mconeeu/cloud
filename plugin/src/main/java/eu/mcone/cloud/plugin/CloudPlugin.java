/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin;

import eu.mcone.cloud.api.plugin.CloudAPI;
import eu.mcone.cloud.core.messaging.URIs;
import eu.mcone.cloud.core.packet.CloudInfoResponsePacket;
import eu.mcone.cloud.core.packet.ServerListUpdatePacketPlugin;
import eu.mcone.cloud.core.packet.ServerRegisterPacketPlugin;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.plugin.handler.ServerListUpdateHandler;
import eu.mcone.networkmanager.api.messaging.request.ClientMessageRequestListener;
import eu.mcone.networkmanager.api.messaging.response.CustomClientMessageResponseListener;
import eu.mcone.networkmanager.api.packet.ClientMessageRequestPacket;
import eu.mcone.networkmanager.client.ClientBootstrap;
import eu.mcone.networkmanager.client.NetworkmanagerClient;
import eu.mcone.networkmanager.client.api.PacketManager;
import group.onegaming.networkmanager.api.packet.Packet;
import group.onegaming.networkmanager.client.ClientBootstrap;
import group.onegaming.networkmanager.client.NetworkmanagerClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class CloudPlugin extends CloudAPI implements NetworkmanagerClient {

    @Getter
    private static CloudPlugin cloudPlugin;

    @Getter
    private eu.mcone.cloud.api.plugin.CloudPlugin plugin;
    @Getter
    private ClientBootstrap nettyClient;
    @Getter
    private String serverName, hostname;
    @Getter @Setter
    private ServerState state = ServerState.WAITING;
    @Getter
    private ServerVersion version;
    @Getter
    private boolean staticServer;
    @Getter
    private List<CloudWorld> loadedWorlds;
    @Getter
    private UUID serverUuid, wrapperUuid;
    @Getter
    private int port;

    public CloudPlugin(eu.mcone.cloud.api.plugin.CloudPlugin plugin) {
        setInstance(this);
        cloudPlugin = this;

        this.plugin = plugin;
        this.loadedWorlds = new ArrayList<>();

        try {
            Properties ps = new Properties();
            FileInputStream fis = new FileInputStream(new File("server.properties"));
            ps.load(fis);

            serverName = ps.getProperty("server-name");
            serverUuid = UUID.fromString(ps.getProperty("server-uuid"));
            wrapperUuid = UUID.fromString(ps.getProperty("wrapper-uuid"));
            hostname = ps.getProperty("wrapper-ip");
            port = Integer.valueOf(ps.getProperty("server-port"));
            version = ServerVersion.valueOf(ps.getProperty("server-version"));
            staticServer = Boolean.valueOf(ps.getProperty("static-server"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        nettyClient = new ClientBootstrap("localhost", "eu.mcone.cloud.plugin", this);

        System.out.println("CloudPlugin finnally enabled!");
    }

    public void unload() {
        nettyClient.getPacketManager().getChannel().close();
    }

    @Override
    public void runAsync(Runnable runnable) {
        plugin.runAsync(runnable);
    }

    @Override
    public void onChannelActive(ChannelHandlerContext chc) {
        nettyClient.getPacketManager().registerPacketHandler(ServerListUpdatePacketPlugin.class, new ServerListUpdateHandler());
        ServerListUpdateHandler.setNewConnection(true);

        chc.writeAndFlush(new ServerRegisterPacketPlugin(
                serverUuid,
                wrapperUuid,
                hostname,
                port,
                state,
                version,
                staticServer,
                plugin.getPlayers()
        ));
    }

    @Override
    public void onChannelUnregistered(ChannelHandlerContext channelHandlerContext) {}

    @Override
    public PacketManager getPacketManager() {
        return nettyClient.getPacketManager();
    }

    @Override
    public void registerClientMessageListener(String uri, ClientMessageRequestListener listener) throws IllegalStateException {
        nettyClient.getMessageManager().registerClientMessageListener(uri, listener);
    }

    @Override
    public void getCloudInfo(CustomClientMessageResponseListener<CloudInfoResponsePacket> callback) {
        nettyClient.getPacketManager().sendClientRequest(new ClientMessageRequestPacket(URIs.CLOUD_INFO, HttpMethod.GET), callback);
    }

}
