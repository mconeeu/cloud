/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.ServerListUpdatePacketPlugin;
import eu.mcone.cloud.core.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerUpdateStateHandler implements PacketHandler<ServerUpdateStatePacket> {

    @Override
    public void onPacketReceive(ServerUpdateStatePacket packet, ChannelHandlerContext chc) {
        log.fine("new ServerUpdateStatePacketPlugin (UUID: "+packet.getUuid()+", STATE: "+packet.getState().toString()+")");
        ServerState state = packet.getState();
        Server s = MasterServer.getInstance().getServer(packet.getUuid());

        if (s != null) {
            s.setState(state);

            boolean busy = false;
            for (Server ser : s.getWrapper().getServers()) {
                if (busy) break;
                busy = ser.getState().equals(ServerState.STARTING);
            }
            log.info("["+s.getWrapper().getUuid()+"] Wrapper is busy? "+busy);
            s.getWrapper().setBusy(busy);

            if (!s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                switch (state) {
                    case WAITING: {
                        for (Server server : MasterServer.getInstance().getServers()) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
                                System.out.println("registered Server "+s.getInfo().getName()+ " @ Bungee "+server.getInfo().getName());
                            }
                        }
                        break;
                    }
                    case OFFLINE: {
                        for (Server server : MasterServer.getInstance().getServers()) {
                            if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                System.out.println("deleting Server "+s.getInfo().getName()+ " @ Bungee "+server.getInfo().getName());
                                server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.REMOVE));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

}
