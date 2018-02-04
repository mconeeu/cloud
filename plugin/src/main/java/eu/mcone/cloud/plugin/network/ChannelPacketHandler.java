/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.plugin.CloudPlugin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.md_5.bungee.api.ProxyServer;

import java.net.InetSocketAddress;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private CloudPlugin instance;

    ChannelPacketHandler(CloudPlugin instance) {
        this.instance = instance;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        instance.setChannel(ctx.channel());
        ctx.writeAndFlush(new ServerRegisterPacketPlugin(instance.getServerUuid(), instance.getHostname(), instance.getPort()));
        System.out.println("new channel to " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Unregister");
        super.channelUnregistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) {
        if (packet instanceof ServerListPacketAddPlugin) {
            ServerListPacketAddPlugin result = (ServerListPacketAddPlugin) packet;
            System.out.println("new ServerUpdateStatePacketPlugin (NAME: "+result.getName()+", HOSTNAME: "+result.getHostname()+":"+result.getPort()+")");

            ProxyServer.getInstance().getServers().put(
                    result.getName(),
                    ProxyServer.getInstance().constructServerInfo(
                            result.getName(),
                            InetSocketAddress.createUnresolved(result.getHostname(), result.getPort()),
                            "§f§lMC ONE §3Server §8» §7"+result.getName(),
                            false
                    )
            );
        } else if (packet instanceof ServerListPacketRemovePlugin) {
            ServerListPacketRemovePlugin result = (ServerListPacketRemovePlugin) packet;
            System.out.println("new ServerListPacketRemovePlugin (NAME: "+result.getName()+")");
            ProxyServer.getInstance().getServers().remove(result.getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        System.out.println("Close Channel");
    }

}
