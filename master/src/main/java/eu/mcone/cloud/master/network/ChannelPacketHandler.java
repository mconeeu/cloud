/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.wrapper.Wrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.*;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.log(getClass(), "new channel from " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof WrapperRegisterPacketWrapper) {
            WrapperRegisterPacketWrapper result = (WrapperRegisterPacketWrapper) packet;
            Logger.log(getClass(), "new WrapperRegisterPacketWrapper (RAM: "+result.getRam()+")");
            new Wrapper(ctx.channel(), result.getRam());
        } else if (packet instanceof WrapperRegisterFromStandalonePacketWrapper) {
            WrapperRegisterFromStandalonePacketWrapper result = (WrapperRegisterFromStandalonePacketWrapper) packet;
            Logger.log(getClass(), "new WrapperRegisterFromStandalonePacketWrapper");
            Logger.log("WrapperRegister", "Wrapper registering with still "+result.getServers().size()+" servers running!");

            Map<UUID, Long> unknown = new HashMap<>();
            List<Server> newServer = new ArrayList<>();
            for (HashMap.Entry<UUID, Long> e : result.getServers().entrySet()) {
                UUID uuid = e.getKey();
                long ram = e.getValue();
                Server s = MasterServer.getInstance().getServer(uuid);

                if (s == null) {
                    unknown.put(uuid, ram);
                } else {
                    MasterServer.getInstance().getServerManager().removeFromServerWaitList(s);
                    s.getWrapper().send(new ServerInfoPacket(s.getInfo()));
                    newServer.add(s);
                }
            }

            Wrapper w = new Wrapper(ctx.channel(), result.getRam());

            Logger.log("WrapperRegister", "Found "+newServer.size()+" valid servers!");
            for (Server s : newServer) {
                MasterServer.getInstance().getServerManager().removeFromServerWaitList(s);
                s.setWrapper(w);
            }

            Logger.log("WrapperRegister", "Found "+unknown.size()+" invalid servers! Deleting from Wrapper...");
            for (HashMap.Entry<UUID, Long> e : unknown.entrySet()) {
                w.deleteServer(e.getKey(), e.getValue());
            }

            Logger.log("WrapperRegister", w.getName()+" is successfully registered from Standalone Mode!");
        } else if (packet instanceof ServerRegisterPacketPlugin) {
            ServerRegisterPacketPlugin result = (ServerRegisterPacketPlugin) packet;
            Logger.log(getClass(), "new ServerRegisterPacketPlugin (UUID: "+result.getServerUuid()+", PORT: "+result.getPort()+")");
            Server s = MasterServer.getInstance().getServer(result.getServerUuid());

            if (s != null) {
                s.setChannel(ctx.channel());
                s.getInfo().setHostname(result.getHostname());
                s.getInfo().setPort(result.getPort());

                if (s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                    for (Server server : MasterServer.getInstance().getServers()) {
                        if (!server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                            s.send(new ServerListPacketAddPlugin(server.getInfo()));
                        }
                    }
                }
            }

        } else if (packet instanceof ServerUpdateStatePacketWrapper) {
            ServerUpdateStatePacketWrapper result = (ServerUpdateStatePacketWrapper) packet;
            Logger.log(getClass(), "new ServerUpdateStatePacketPlugin (UUID: "+result.getUuid()+", STATE: "+result.getState().toString()+")");
            ServerState state = result.getState();
            Server s = MasterServer.getInstance().getServer(result.getUuid());

            if (s != null) {
                s.setState(state);

                boolean busy = false;
                for (Server ser : s.getWrapper().getServers()) {
                    if (busy) break;
                    busy = ser.getState().equals(ServerState.STARTING);
                }
                Logger.log(getClass(), "["+s.getWrapper().getName()+"] Wrapper is busy? "+busy);
                s.getWrapper().setBusy(busy);

                switch (state) {
                    case WAITING: {
                        for (Server server : MasterServer.getInstance().getServers()) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                server.send(new ServerListPacketAddPlugin(s.getInfo()));
                            }
                        }
                        break;
                    }
                    case OFFLINE: {
                        for (Server server : MasterServer.getInstance().getServers()) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                server.send(new ServerListPacketRemovePlugin(s.getInfo()));
                            }
                        }
                        break;
                    }
                }
            }
        } else if (packet instanceof ServerResultPacketWrapper) {
            ServerResultPacketWrapper result = (ServerResultPacketWrapper) packet;
            Logger.log(getClass(), "[" + result.getResultClass() + "] " + result.getMessage() + " ResultType: " + result.getResult());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress()+" unregistered");
        for (Wrapper w : MasterServer.getInstance().getWrappers()) {
            if (w.getChannel().equals(ctx.channel())) {
                w.delete();
                return;
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
