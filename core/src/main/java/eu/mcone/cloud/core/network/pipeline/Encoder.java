/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.pipeline;

import eu.mcone.cloud.core.network.Protocol;
import eu.mcone.cloud.core.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        System.out.println("encoding packet "+packet.getClass());
        int packetID = Protocol.getIDByClass(packet.getClass());

        if (packetID > -1) {
            out.writeInt(packetID);
            packet.write(out);
        }
    }
}
