/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerRegisterPacketPlugin extends Packet {

    @Getter
    private UUID serverUuid;
    @Getter
    private String hostname;
    @Getter
    private int port;

    public ServerRegisterPacketPlugin() {}

    public ServerRegisterPacketPlugin(UUID serverUuid, String hostname, int port) {
        this.serverUuid = serverUuid;
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(serverUuid.toString());
            out.writeUTF(hostname);
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
            hostname = input.readUTF();
            port = input.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
