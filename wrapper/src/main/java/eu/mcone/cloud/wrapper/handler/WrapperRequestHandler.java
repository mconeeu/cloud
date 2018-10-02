/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.handler;

import eu.mcone.cloud.core.packet.WrapperRequestPacketMaster;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperRequestHandler implements PacketHandler<WrapperRequestPacketMaster> {

    @Override
    public void onPacketReceive(WrapperRequestPacketMaster packet, ChannelHandlerContext chc) {
        /*log.fine("new WrapperRequestPacketMaster (OBJECT: "+packet.getType()+", VALUE: "+packet.getRequest()+")");
        Server s = WrapperServer.getInstance().getServer(UUID.fromString(packet.getRequest()));

        if (s != null) {
            switch (packet.getType()) {
                case LOG: {
                    chc.writeAndFlush(new ServerLogPacketClient(packet.getRequest(), s.getInfo().getUuid(), s.getReader().getLogList()));
                    break;
                }
            }
        } else {
            log.severe("server does not exist!");
        }*/
    }

}
