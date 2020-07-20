/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.ServerPlayerCountUpdatePacketPlugin;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerPlayerCountUpdateHandler implements PacketHandler<ServerPlayerCountUpdatePacketPlugin> {

    @Override
    public void onPacketReceive(ServerPlayerCountUpdatePacketPlugin packet, ChannelHandlerContext chc) {
        log.fine("new ServerPlayerCountUpdatePacketPlugin (UUID: "+packet.getUuid()+", COUNT: "+packet.getPlayerCount()+")");
        Server s = MasterServer.getInstance().getServer(packet.getUuid());

        if (s != null) {
            s.setPlayerCount(packet.getPlayerCount());
        } else {
            log.warning("Playercount for Server with UUID "+packet.getUuid()+" could not be changed! (Server does not exist)");
        }
    }

}
