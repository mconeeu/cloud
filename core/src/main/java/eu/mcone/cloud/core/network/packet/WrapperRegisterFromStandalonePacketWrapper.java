/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.*;

public class WrapperRegisterFromStandalonePacketWrapper extends Packet {

    @Getter
    private Long ram;
    @Getter
    private Map<UUID, Long> servers;

    public WrapperRegisterFromStandalonePacketWrapper() {}

    public WrapperRegisterFromStandalonePacketWrapper(Map<UUID, Long> servers, long ram) {
        this.ram = ram;
        this.servers = servers;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeLong(ram);
            out.writeInt(servers.size());
            for (HashMap.Entry<UUID, Long> e : servers.entrySet()) {
                out.writeUTF(e.getKey().toString());
                out.writeLong(e.getValue());
            }

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
            int size = input.readInt();
            servers = new HashMap<>();
            for (int i = 1; i >= size; i++) {
                servers.put(UUID.fromString(input.readUTF()), input.readLong());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
