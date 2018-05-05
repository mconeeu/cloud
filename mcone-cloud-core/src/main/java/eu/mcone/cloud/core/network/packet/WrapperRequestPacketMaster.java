/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class WrapperRequestPacketMaster extends Packet {

    @Getter
    private Object object;
    @Getter
    private UUID request;
    @Getter
    private String value;

    public enum Object {
        LOG
    }

    public WrapperRequestPacketMaster() {}

    public WrapperRequestPacketMaster(Object object, UUID request, String value) {
        this.object = object;
        this.request = request;
        this.value = value;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(object.toString());
            out.writeUTF(request.toString());
            out.writeUTF(value);

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
            object = Object.valueOf(input.readUTF());
            request = UUID.fromString(input.readUTF());
            value = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
