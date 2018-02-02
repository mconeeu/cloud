/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        WrapperServer.getInstance().setChannel(ctx.channel());
        ctx.writeAndFlush(new WrapperRegisterPacket(WrapperServer.getInstance().getRam()));
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
            ServerInfoPacket result = (ServerInfoPacket) packet;
            ServerInfo info = result.getServerInfo();
            System.out.println("new ServerInfoPacket (UUID: "+result.getServerInfo().getUuid()+", NAME: "+result.getServerInfo().getName()+")");

            for (Server s : WrapperServer.getInstance().getServers()) {
                if (s.getInfo().getUuid().equals(info.getUuid())) {
                    s.setInfo(info);
                    return;
                }
            }

            new Server(result.getServerInfo());
        } else if (packet instanceof ServerChangeStatePacket) {
            ServerChangeStatePacket result = (ServerChangeStatePacket) packet;
            System.out.println("new ServerChangeStatePacket (UUID: "+result.getServerUuid()+", STATE: "+result.getState().toString()+")");

            Server s = WrapperServer.getInstance().getServer(result.getServerUuid());

            if (s != null) {
                switch (result.getState()) {
                    case START: s.start();
                    //case STOP: s.stop();
                    //case FORCESTOP: s.forceStop();
                    //case RESTART: s.restart();
                    //case DELETE: s.delete();
                }
            } else {
                System.out.println("s == null");
            }
        } else if (packet instanceof ServerCommandExecutePacket) {
            ServerCommandExecutePacket result = (ServerCommandExecutePacket) packet;
            System.out.println("new ServerCommandExecutePacket (UUID: "+result.getServerUuid()+", COMMAND: "+result.getCmd()+")");

            Server s = WrapperServer.getInstance().getServer(result.getServerUuid());
            if (s != null) {
                s.sendcommand(result.getCmd());
            } else {
                System.out.println("s == null");
            }
        } else if (packet instanceof WrapperShutdownPacket) {
            System.out.println("[ChannelPacketHandler] Received WrapperShutdownPacket from master. Shutting down...");
            WrapperServer.getInstance().shutdown();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        System.out.println("Close Channel");
    }

}
