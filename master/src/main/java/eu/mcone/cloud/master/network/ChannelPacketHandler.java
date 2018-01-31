/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.wrapper.Wrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("new channel from " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof WrapperRegisterPacket) {
            WrapperRegisterPacket result = (WrapperRegisterPacket) packet;
            System.out.println("new WrapperRegisterPacket (RAM: "+result.getRam()+")");
            new Wrapper(ctx.channel(), result.getRam());
        } else if (packet instanceof ServerRegisterPacket) {
            ServerRegisterPacket result = (ServerRegisterPacket) packet;
            System.out.println("new ServerRegisterPacket (UUID: "+result.getServerUuid()+", PORT: "+result.getPort()+")");
            Server s = MasterServer.getInstance().getServer(result.getServerUuid());

            if (s != null) {
                s.setChannel(ctx.channel());
                s.setPort(result.getPort());
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress()+" unregistered");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        System.out.println("Close Channel");
    }

}
