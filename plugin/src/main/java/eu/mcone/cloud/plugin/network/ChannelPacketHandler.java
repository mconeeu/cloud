/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.network;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerListUpdatePacketPlugin;
import eu.mcone.cloud.core.network.packet.ServerRegisterPacketPlugin;
import eu.mcone.cloud.plugin.CloudPlugin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.md_5.bungee.api.ProxyServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private CloudPlugin instance;
    private boolean newConnection = false;

    ChannelPacketHandler(CloudPlugin instance) {
        this.instance = instance;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        instance.setChannel(ctx.channel());
        newConnection = true;
        ctx.writeAndFlush(new ServerRegisterPacketPlugin(instance.getServerUuid(), instance.getHostname(), instance.getPort(), instance.getPlugin().getPlayerCount(), instance.getServerState()));
        System.out.println("new channel to " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (newConnection) {
            ProxyServer.getInstance().getServers().clear();
            newConnection = false;
        }

        if (packet instanceof ServerListUpdatePacketPlugin) {
            ServerListUpdatePacketPlugin result = (ServerListUpdatePacketPlugin) packet;
            System.out.println("new ServerListUpdatePacketPlugin (NAME: "+result.getName()+", HOSTNAME: "+result.getHostname()+":"+result.getPort()+")");

            if (result.getScope().equals(ServerListUpdatePacketPlugin.Scope.ADD)) {
                System.out.println("adding server " + result.getName() + " to bc server map");
                ProxyServer.getInstance().getServers().put(
                        result.getName(),
                        ProxyServer.getInstance().constructServerInfo(
                                result.getName(),
                                InetSocketAddress.createUnresolved(result.getHostname(), result.getPort()),
                                "§f§lMC ONE §3Server §8» §7" + result.getName(),
                                false
                        )
                );
            } else if (result.getScope().equals(ServerListUpdatePacketPlugin.Scope.REMOVE)) {
                ProxyServer.getInstance().getServers().remove(result.getName());
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        instance.getNettyBootstrap().scheduleReconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            System.err.println(cause.getMessage());
            System.err.println("Reconnecting...");
            return;
        }

        System.err.println("Netty Exception:");
        cause.printStackTrace();
    }

}
