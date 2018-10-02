/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.WrapperRequestPacketMaster;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperRequestHandler implements PacketHandler<WrapperRequestPacketMaster> {

    @Override
    public void onPacketReceive(WrapperRequestPacketMaster packet, ChannelHandlerContext chc) {

    }

}
