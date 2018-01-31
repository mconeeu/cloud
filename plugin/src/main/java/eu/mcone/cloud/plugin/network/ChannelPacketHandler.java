/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.plugin.CloudPlugin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private CloudPlugin instance;

    public ChannelPacketHandler(CloudPlugin instance) {
        this.instance = instance;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        instance.setChannel(ctx.channel());
        ctx.writeAndFlush(new ServerRegisterPacket(instance.getServerUuid(), instance.getServerPort()));
        System.out.println("new channel to " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Unregister");
        super.channelUnregistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) {
        System.out.println("New packet: " + packet.toString());

        if (packet instanceof ServerInfoPacket) {

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        System.out.println("Close Channel");
    }

}
