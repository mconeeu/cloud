package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacket;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.master.MasterServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;

/**
 * Created with IntelliJ IDE
 * Created on 28.01.2018
 * Copyright (c) 2018 Dominik L. All rights reserved
 * You are not allowed to decompile the code
 */
public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        MasterServer.connections.add(ctx);
        System.out.println("new channel from " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        if (packet instanceof ServerInfoPacket) {
            ServerInfoPacket result = (ServerInfoPacket) packet;
            System.out.println("new ServerInfo received: " + result.getServerInfo().getName());
        } else if (packet instanceof ServerCommandExecutePacket) {
            ServerCommandExecutePacket result = (ServerCommandExecutePacket) packet;
            System.out.println("new ServerCommandExecutePacket received: " + result.getCmd());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
