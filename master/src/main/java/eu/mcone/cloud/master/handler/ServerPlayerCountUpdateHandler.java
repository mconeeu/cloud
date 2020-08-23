/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.ServerPlayerCountUpdatePacketPlugin;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.CloudServer;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerPlayerCountUpdateHandler implements PacketHandler<ServerPlayerCountUpdatePacketPlugin> {

    @Override
    public void onPacketReceive(ServerPlayerCountUpdatePacketPlugin packet, ChannelHandlerContext chc) {
        log.fine("new ServerPlayerCountUpdatePacketPlugin (UUID: "+packet.getServerUuid()+", METHOD: "+packet.getMethod()+", : PLAYER:"+packet.getName()+")");
        CloudServer s = (CloudServer) MasterServer.getServer().getServer(packet.getServerUuid());

        if (s != null) {
            switch (packet.getMethod()) {
                case ADD: s.addPlayer(packet.getUuid(), packet.getName()); break;
                case REMOVE: s.removePlayer(packet.getUuid()); break;
            }
        } else {
            log.warning("Playercount for Server with UUID "+packet.getServerUuid()+" could not be changed! (Server does not exist)");
        }
    }

}
