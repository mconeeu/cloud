package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

/**
 * Created with IntelliJ IDE
 * Created on 29.01.2018
 * Copyright (c) 2018 Dominik L. All rights reserved
 * You are not allowed to decompile the code
 */
public class ServerStartPacket extends Packet{

    @Getter
    private UUID serverUuid;

    @Getter
    private String cmd;

    public ServerStartPacket() {}

    public ServerStartPacket(UUID serverUuid, String cmd) {
        this.serverUuid = serverUuid;
        this.cmd = cmd;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(serverUuid.toString());
            out.writeUTF(cmd);

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
            cmd = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
