/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Bungee;
import eu.mcone.cloud.wrapper.server.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        WrapperServer.getInstance().setChannel(ctx.channel());
        ctx.writeAndFlush(new WrapperRegisterPacketWrapper(WrapperServer.getInstance().getRam()));
        System.out.println("new channel to " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Unregister");
        super.channelUnregistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof ServerInfoPacket) {
            ServerInfoPacket result = (ServerInfoPacket) packet;
            ServerInfo info = result.getServerInfo();
            System.out.println("new ServerInfoPacket (UUID: "+result.getServerInfo().getUuid()+", NAME: "+result.getServerInfo().getName()+")");

            if(result.getServerInfo().getVersion().equals(ServerVersion.BUNGEE)){
                for (Bungee b : WrapperServer.getInstance().getBungees()) {
                    if (b.getInfo().getUuid().equals(info.getUuid())) {
                        b.setInfo(info);
                        return;
                    }
                }

                new Bungee(result.getServerInfo());
            }else if(result.getServerInfo().getVersion().equals(ServerVersion.BUKKIT)){
                for (Server s : WrapperServer.getInstance().getServers()) {
                    if (s.getInfo().getUuid().equals(info.getUuid())) {
                        s.setInfo(info);
                        return;
                    }
                }

                new Server(result.getServerInfo());
            }
        } else if (packet instanceof ServerChangeStatePacketWrapper) {
            ServerChangeStatePacketWrapper result = (ServerChangeStatePacketWrapper) packet;
            System.out.println("new ServerChangeStatePacketWrapper (UUID: "+result.getServerUuid()+", STATE: "+result.getState().toString()+")");

            Server s = WrapperServer.getInstance().getServer(result.getServerUuid());
            Bungee b = WrapperServer.getInstance().getBungee(result.getServerUuid());

            if(b != null){
                switch (result.getState()){
                    case START: b.start(); break;
                    case STOP: b.stop(); break;
                    case FORCESTOP: b.forceStop(); break;
                    case DELETE: b.delete(); break;
                }
            }else if (s != null) {
                switch (result.getState()) {
                    case START: s.start(); break;
                    case STOP: s.stop(); break;
                    case FORCESTOP: s.forceStop(); break;
                    case RESTART: s.restart(); break;
                    case DELETE: s.delete(); break;
                }
            } else{

            }
        } else if (packet instanceof ServerCommandExecutePacketWrapper) {
            ServerCommandExecutePacketWrapper result = (ServerCommandExecutePacketWrapper) packet;
            System.out.println("new ServerCommandExecutePacketWrapper (UUID: "+result.getServerUuid()+", COMMAND: "+result.getCmd()+")");

            Server s = WrapperServer.getInstance().getServer(result.getServerUuid());
            if (s != null) {
                s.sendcommand(result.getCmd());
            } else {
                System.out.println("s == null");
            }
        } else if (packet instanceof WrapperShutdownPacketWrapper) {
            System.out.println("[ChannelPacketHandler] Received WrapperShutdownPacketWrapper from master. Shutting down...");
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
