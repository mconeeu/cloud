/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import eu.mcone.cloud.core.server.ServerState;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerUpdateStatePacket extends Packet {

    @Getter
    private UUID uuid;
    @Getter
    private ServerState state;

    public ServerUpdateStatePacket() {}

    public ServerUpdateStatePacket(UUID uuid, ServerState state) {
        this.uuid = uuid;
        this.state = state;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(uuid.toString());
            out.writeUTF(state.toString());

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
            uuid = UUID.fromString(input.readUTF());
            state = ServerState.valueOf(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
