/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.network;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerInfo;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Bukkit;
import eu.mcone.cloud.wrapper.server.BungeeCord;
import eu.mcone.cloud.wrapper.server.Server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        WrapperServer.getInstance().setChannel(ctx.channel());

        if (WrapperServer.getInstance().getServers().size() < 1) {
            ctx.writeAndFlush(new WrapperRegisterPacketWrapper(WrapperServer.getInstance().getRam(), WrapperServer.getInstance().getWrapperUuid()));
        } else {
            Map<UUID, String> servers = new HashMap<>();
            WrapperServer.getInstance().getServers().forEach(server -> servers.put(server.getInfo().getUuid(), server.getInfo().getName()));

            ctx.writeAndFlush(new WrapperRegisterFromStandalonePacketWrapper(servers, WrapperServer.getInstance().getRam(), WrapperServer.getInstance().getWrapperUuid()));
        }
        Logger.log(getClass(), "new channel to " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof ServerInfoPacket) {
            ServerInfoPacket result = (ServerInfoPacket) packet;
            ServerInfo info = result.getServerInfo();
            Logger.log(getClass(), "new ServerInfoPacket (UUID: "+result.getServerInfo().getUuid()+", NAME: "+result.getServerInfo().getName()+")");

            for (Server s : WrapperServer.getInstance().getServers()) {
                if (s.getInfo().getUuid().equals(info.getUuid())) {
                    s.setInfo(info);
                    return;
                }
            }

            switch (result.getServerInfo().getVersion()) {
                case BUNGEE: new BungeeCord(result.getServerInfo()); break;
                case SPIGOT: case BUKKIT: new Bukkit(result.getServerInfo()); break;
            }
        } else if (packet instanceof ServerChangeStatePacketWrapper) {
            ServerChangeStatePacketWrapper result = (ServerChangeStatePacketWrapper) packet;
            Logger.log(getClass(), "new ServerChangeStatePacketWrapper (UUID: "+result.getServerUuid()+", STATE: "+result.getState().toString()+")");

            Server s = WrapperServer.getInstance().getServer(result.getServerUuid());

            switch (result.getState()) {
                case START: s.start(); break;
                case STOP: s.stop(); break;
                case FORCESTOP: s.forcestop(); break;
                case RESTART: s.restart(); break;
                case DELETE: s.delete(); break;
            }
        } else if (packet instanceof ServerCommandExecutePacketWrapper) {
            ServerCommandExecutePacketWrapper result = (ServerCommandExecutePacketWrapper) packet;
            Logger.log(getClass(), "new ServerCommandExecutePacketWrapper (UUID: "+result.getServerUuid()+", COMMAND: "+result.getCmd()+")");

            Server s = WrapperServer.getInstance().getServer(result.getServerUuid());
            if (s != null) {
                s.sendCommand(result.getCmd());
            } else {
                Logger.log(getClass(), "s == null");
            }
        } else if (packet instanceof WrapperShutdownPacketWrapper) {
            Logger.log(getClass(), "[ChannelPacketHandler] Received WrapperShutdownPacketWrapper from master. Shutting down...");
            WrapperServer.getInstance().shutdown();
        } else if (packet instanceof WrapperRequestPacketMaster) {
            WrapperRequestPacketMaster result = (WrapperRequestPacketMaster) packet;
            Logger.log(getClass(), "new WrapperRequestPacketMaster (OBJECT: "+result.getObject()+", VALUE: "+result.getValue()+")");
            Server s = WrapperServer.getInstance().getServer(UUID.fromString(result.getValue()));

            if (s != null) {
                switch (result.getObject()) {
                    case LOG: {
                        ctx.writeAndFlush(new ServerLogPacketClient(result.getRequest(), s.getInfo().getUuid(), s.getReader().getLog()));
                        break;
                    }
                }
            } else {
                Logger.err(getClass(), "server does not exist!");
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        if (!WrapperServer.getInstance().isShutdown()) WrapperServer.getInstance().getNettyBootstrap().scheduleReconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            Logger.err(getClass(), cause.getMessage());
            Logger.err(getClass(), "Reconnecting...");
            return;
        }

        Logger.err(getClass(), "Netty Exception:");
        cause.printStackTrace();
    }

}
