/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerRegisterPacket extends Packet {

    @Getter
    private UUID serverUuid;
    @Getter
    private int port;

    public ServerRegisterPacket() {}

    public ServerRegisterPacket(UUID serverUuid, int port) {
        this.serverUuid = serverUuid;
        this.port = port;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(serverUuid.toString());
            out.writeInt(port);

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
            serverUuid = UUID.fromString(input.readUTF());
            port = input.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
