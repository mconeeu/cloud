/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerInfo;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;

public class ServerListPacketRemovePlugin extends Packet {

    @Getter
    private String name;

    public ServerListPacketRemovePlugin() {}

    public ServerListPacketRemovePlugin(ServerInfo info) {
        this.name = info.getName();
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(name);

            byte[] result = stream.toByteArray();
            byteBuf.writeInt(result.length);
            byteBuf.writeBytes(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(ByteBuf byteBuf) {
        byte[] msg = new byte[byteBuf.readInt()];
        byteBuf.readBytes(msg);

        DataInputStream input = new DataInputStream(new ByteArrayInputStream(msg));
        try {
            name = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
