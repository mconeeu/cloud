/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.wrapper.Wrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Pack200;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("new channel from " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        System.out.println("New packet: " + packet.toString());
        if (packet instanceof ServerInfoPacket) {
            ServerInfoPacket result = (ServerInfoPacket) packet;
            System.out.println("new ServerInfo received: " + result.getServerInfo().getName());

        } else if (packet instanceof WrapperRegisterPacket) {
            WrapperRegisterPacket result = (WrapperRegisterPacket) packet;
            new Wrapper(ctx.channel(), result.getRam());

        }else if(packet instanceof ServerResultPacket){
            //ServerResultPacket result = (ServerResultPacket) packet;
            //System.out.println("[" + result.getResultClass() + "] " + result.getMessage() + " ResultType: " + result.getResult());
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
