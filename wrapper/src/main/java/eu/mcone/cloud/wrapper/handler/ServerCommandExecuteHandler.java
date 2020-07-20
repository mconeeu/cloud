/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.handler;

import eu.mcone.cloud.core.packet.ServerCommandExecutePacketWrapper;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Server;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerCommandExecuteHandler implements PacketHandler<ServerCommandExecutePacketWrapper> {

    @Override
    public void onPacketReceive(ServerCommandExecutePacketWrapper packet, ChannelHandlerContext chc) {
        log.fine("new ServerCommandExecutePacketWrapper (UUID: "+packet.getServerUuid()+", COMMAND: "+packet.getCmd()+")");

        Server s = WrapperServer.getInstance().getServer(packet.getServerUuid());
        if (s != null) {
            s.sendCommand(packet.getCmd());
        } else {
            log.info("s == null");
        }
    }

}
