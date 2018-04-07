/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
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
    private UUID uuid;
    @Getter
    private Map<UUID, String> servers;

    public WrapperRegisterFromStandalonePacketWrapper() {}

    public WrapperRegisterFromStandalonePacketWrapper(Map<UUID, String> servers, long ram, UUID uuid) {
        this.ram = ram;
        this.uuid = uuid;
        this.servers = servers;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeLong(ram);
            out.writeUTF(uuid.toString());
            out.writeInt(servers.size());
            for (HashMap.Entry<UUID, String> e : servers.entrySet()) {
                out.writeUTF(e.getKey().toString());
                out.writeUTF(e.getValue());
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
            uuid = UUID.fromString(input.readUTF());

            int size = input.readInt();
            servers = new HashMap<>();

            for (int i = 0; i < size; i++) {
                UUID uuid = UUID.fromString(input.readUTF());
                String name = input.readUTF();

                servers.put(uuid, name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
