package eu.mcone.cloud.wrapper.network;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerCommandExecutePacket;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Server;
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
        WrapperServer.connections.add(ctx);
        System.out.println("new channel to " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Unregister");
        super.channelUnregistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        System.out.println("Channel read method in class ChannelPacketHandler WRAPPER");

        System.out.println("New packet: " + packet.toString());
        if (packet instanceof ServerInfoPacket) {
            ServerInfoPacket result = (ServerInfoPacket) packet;
            System.out.println("new ServerInfo received: " + result.getServerInfo().getName());
        } else if (packet instanceof ServerCommandExecutePacket) {
            ServerCommandExecutePacket result = (ServerCommandExecutePacket) packet;
            System.out.println("new ServerCommandExecutePacket received: " + result.getCmd());

            for (Server s : WrapperServer.servers.values()) {
                if (s.getInfo().getUuid().equals(result.getServerUuid())) {
                    s.sendcommand(result.getCmd());
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        System.out.println("Close Channel");
    }

}
