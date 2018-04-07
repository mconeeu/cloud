/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MasterRequestPacketClient extends Packet {

    @Getter
    private String json;

    @Override
    public void write(ByteBuf byteBuf) {}

    @Override
    public void read(ByteBuf byteBuf) {
        byte[] msg = new byte[byteBuf.readInt()];
        byteBuf.readBytes(msg);

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(msg));
        try {
            json = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
