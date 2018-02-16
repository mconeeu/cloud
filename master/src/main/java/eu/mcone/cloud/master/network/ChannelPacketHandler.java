/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
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
        if (packet instanceof WrapperRegisterPacketWrapper) {
            WrapperRegisterPacketWrapper result = (WrapperRegisterPacketWrapper) packet;
            System.out.println("new WrapperRegisterPacketWrapper (RAM: "+result.getRam()+")");
            new Wrapper(ctx.channel(), result.getRam());
        } else if (packet instanceof ServerRegisterPacketPlugin) {
            ServerRegisterPacketPlugin result = (ServerRegisterPacketPlugin) packet;
            System.out.println("new ServerRegisterPacketPlugin (UUID: "+result.getServerUuid()+", PORT: "+result.getPort()+")");
            Server s = MasterServer.getInstance().getServer(result.getServerUuid());

            if (s != null) {
                s.setChannel(ctx.channel());
                s.getInfo().setState(ServerState.STARTING);
                s.getInfo().setHostname(result.getHostname());
                s.getInfo().setPort(result.getPort());

                if (s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                    for (Server server : MasterServer.getInstance().getServers()) {
                        if (!server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getInfo().getState().equals(ServerState.OFFLINE)) {
                            s.send(new ServerListPacketAddPlugin(server.getInfo()));
                        }
                    }
                }
            }

        } else if (packet instanceof ServerUpdateStatePacketPlugin) {
            ServerUpdateStatePacketPlugin result = (ServerUpdateStatePacketPlugin) packet;
            System.out.println("new ServerUpdateStatePacketPlugin (UUID: "+result.getUuid()+", STATE: "+result.getState().toString()+")");
            ServerState state = result.getState();
            Server s = MasterServer.getInstance().getServer(result.getUuid());

            if (s != null) {
                s.getInfo().setState(state);

                switch (state) {
                    case WAITING: {
                        for (Server server : MasterServer.getInstance().getServers()) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getInfo().getState().equals(ServerState.OFFLINE)) {
                                server.send(new ServerListPacketAddPlugin(s.getInfo()));
                            }
                        }
                        break;
                    }
                    case OFFLINE: {
                        for (Server server : MasterServer.getInstance().getServers()) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getInfo().getState().equals(ServerState.OFFLINE)) {
                                server.send(new ServerListPacketRemovePlugin(s.getInfo()));
                            }
                        }
                        break;
                    }
                }
            }
        } else if(packet instanceof ServerProgressStatePacketMaster){
            ServerProgressStatePacketMaster result = (ServerProgressStatePacketMaster) packet;
            System.out.println("[" + result.getEvent_class() + "] Received new ProgressState Packet '" + result.getProgress() + "'");
                for(Wrapper wrapper : MasterServer.getInstance().getWrappers()){
                    if(wrapper.getChannel().equals(ctx.channel())){
                        if(result.getProgress().equals(ServerProgressStatePacketMaster.Progress.INPROGRESSING)){
                            wrapper.setProgressing(true);
                        }else if(result.getProgress().equals(ServerProgressStatePacketMaster.Progress.NOTPROGRESSING)){
                            wrapper.setProgressing(false);
                        }
                    }
                }
        }else if (packet instanceof ServerResultPacketWrapper) {
            ServerResultPacketWrapper result = (ServerResultPacketWrapper) packet;
            System.out.println("[" + result.getResultClass() + "] " + result.getMessage() + " ResultType: " + result.getResult());
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
