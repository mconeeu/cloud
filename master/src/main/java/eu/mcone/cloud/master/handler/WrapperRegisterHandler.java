/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.WrapperRegisterPacketWrapper;
import eu.mcone.cloud.master.MasterServer;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperRegisterHandler implements PacketHandler<WrapperRegisterPacketWrapper> {

    @Override
    public void onPacketReceive(WrapperRegisterPacketWrapper packet, ChannelHandlerContext chc) {
        log.fine("new WrapperRegisterPacketWrapper (UUID: "+packet.getUuid()+", RAM: "+packet.getRam()+")");
        MasterServer.getServer().createWrapper(packet.getUuid(), chc.channel(), packet.getRam());
    }

}
