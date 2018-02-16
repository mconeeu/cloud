/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.pipeline;

import eu.mcone.cloud.core.network.Protocol;
import eu.mcone.cloud.core.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int packetID = in.readInt();
        Class<? extends Packet> packetClass = Protocol.getClassbyID(packetID);

        if (packetClass != null) {
            Packet packet = packetClass.newInstance();
            packet.read(in);

            out.add(packet);
        }
    }
}
