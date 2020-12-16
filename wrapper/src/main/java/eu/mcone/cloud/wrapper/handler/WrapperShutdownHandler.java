/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.handler;

import eu.mcone.cloud.core.packet.WrapperShutdownPacketWrapper;
import eu.mcone.cloud.wrapper.WrapperServer;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperShutdownHandler implements PacketHandler<WrapperShutdownPacketWrapper> {

    @Override
    public void onPacketReceive(WrapperShutdownPacketWrapper packet, ChannelHandlerContext chc) {
        log.info("[ChannelPacketHandler] Received WrapperShutdownPacketWrapper from master. Shutting down...");
        WrapperServer.getInstance().shutdown();
    }

}
