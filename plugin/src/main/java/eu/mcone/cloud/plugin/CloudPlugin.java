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
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.plugin.handler.ServerListUpdateHandler;
import eu.mcone.networkmanager.api.packet.Packet;
import eu.mcone.networkmanager.client.ClientBootstrap;
import eu.mcone.networkmanager.client.NetworkmanagerClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
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

    private final static GenericFutureListener<Future<? super Void>> FUTURE_LISTENER = future -> {
        if (!future.isSuccess() || future.isCancelled()) {
            System.err.println("Netty Flush Operation failed:" +
                    "\nisDone ? " + future.isDone() + ", " +
                    "\nisSuccess ? " + future.isSuccess() + ", " +
                    "\ncause : " + future.cause() + ", " +
                    "\nisCancelled ? " + future.isCancelled());
            if (future.cause() != null) future.cause().printStackTrace();
        }
    };

    @Getter
    private eu.mcone.cloud.api.plugin.CloudPlugin plugin;
    @Getter
    private ClientBootstrap nettyBootstrap;
    @Getter @Setter
    private Channel channel;
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

        nettyBootstrap = new ClientBootstrap("localhost", "", this);

        System.out.println("CloudPlugin finnally enabled!");
    }

    public void unload() {
        channel.close();
    }



    @Override
    public void runAsync(Runnable runnable) {
        plugin.runAsync(runnable);
    }

    @Override
    public void onChannelActive(ChannelHandlerContext chc) {
        channel = chc.channel();
        nettyBootstrap.getPacketManager().registerPacketHandler(ServerListUpdatePacketPlugin.class, new ServerListUpdateHandler());
        ServerListUpdateHandler.setNewConnection(true);

        chc.writeAndFlush(new ServerRegisterPacketPlugin(
                serverUuid,
                wrapperUuid,
                hostname,
                port,
                plugin.getPlayerCount(),
                state,
                version,
                staticServer
        ));
    }

    @Override
    public void onChannelUnregistered(ChannelHandlerContext channelHandlerContext) {}

    @Override
    public ChannelFuture send(Packet packet) {
        return channel.writeAndFlush(packet).addListener(FUTURE_LISTENER);
    }
}
