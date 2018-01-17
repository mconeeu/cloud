package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;

public abstract class Packet {
    public abstract void write(ByteBuf byteBuf);
    public abstract void read(ByteBuf byteBuf);
}
