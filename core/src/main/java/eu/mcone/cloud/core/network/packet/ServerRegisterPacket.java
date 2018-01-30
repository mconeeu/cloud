/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

public class ServerRegisterPacket extends Packet {

    @Getter
    private int port;

    public ServerRegisterPacket() {}

    public ServerRegisterPacket(int port) {
        this.port = port;
    }

    /* Todo */

    @Override
    public void write(ByteBuf byteBuf) {

    }

    @Override
    public void read(ByteBuf byteBuf) {

    }

}
