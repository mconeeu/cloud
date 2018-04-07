/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class WrapperRegisterPacketWrapper extends Packet {

    @Getter
    private long ram;
    @Getter
    private UUID uuid;

    public WrapperRegisterPacketWrapper() {}

    public WrapperRegisterPacketWrapper(long ram, UUID uuid) {
        this.ram = ram;
        this.uuid = uuid;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeLong(ram);
            out.writeUTF(uuid.toString());

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
            ram = input.readLong();
            uuid = UUID.fromString(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
