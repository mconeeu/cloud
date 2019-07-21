/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.handler;

import eu.mcone.cloud.core.packet.ServerChangeStatePacketWrapper;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Server;
import eu.mcone.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class ServerChangeStateHandler implements PacketHandler<ServerChangeStatePacketWrapper> {

    @Override
    public void onPacketReceive(ServerChangeStatePacketWrapper packet, ChannelHandlerContext chc) {
        log.fine("new ServerChangeStatePacketWrapper (UUID: "+packet.getServerUuid()+", STATE: "+packet.getState().toString()+")");

        Server s = WrapperServer.getInstance().getServer(packet.getServerUuid());

        switch (packet.getState()) {
            case START: s.start();
                break;
            case STOP: s.stop();
                break;
            case FORCESTOP: s.forcestop();
                break;
            case RESTART: s.restart();
                break;
            case DELETE: s.delete();
                break;
        }
    }

}
