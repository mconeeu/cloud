/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.packet.ServerUpdateStatePacket;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.BungeeServerListUpdater;
import eu.mcone.cloud.master.server.CloudServer;
import eu.mcone.networkmanager.api.packet.interfaces.PacketHandler;
import eu.mcone.cloud.master.server.Server;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerUpdateStateHandler implements PacketHandler<ServerUpdateStatePacket> {

    @Override
    public void onPacketReceive(ServerUpdateStatePacket packet, ChannelHandlerContext chc) {
        log.fine("new ServerUpdateStatePacketPlugin (UUID: " + packet.getUuid() + ", STATE: " + packet.getState().toString() + ")");
        ServerState state = packet.getState();
        CloudServer s = (CloudServer) MasterServer.getServer().getServer(packet.getUuid());

        if (s != null) {
            s.setState(state);

            if (s.getWrapper() != null) {
                boolean busy = false;
                for (Server ser : s.getWrapper().getServers()) {
                    if (busy) break;
                    busy = ser.getState().equals(ServerState.STARTING);
                }
                log.info("[" + s.getWrapper().getUuid() + "] Wrapper is busy? " + busy);
                s.getWrapper().setBusy(busy);
            }

            if (packet.getState().equals(ServerState.OFFLINE) || packet.getState().equals(ServerState.BROKEN) && !s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                BungeeServerListUpdater.unregisterServerOnAllBungees(s);
            }
        }
    }

}
