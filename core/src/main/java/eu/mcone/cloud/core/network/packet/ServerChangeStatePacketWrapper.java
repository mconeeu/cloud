/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.*;
import java.util.UUID;

public class ServerChangeStatePacketWrapper extends Packet {

    @Getter
    private UUID serverUuid;
    @Getter
    private State state;

    public enum State {
        START, STOP, FORCESTOP, RESTART, DELETE
    }

    public ServerChangeStatePacketWrapper() {}

    public ServerChangeStatePacketWrapper(UUID serverUuid, State state) {
        this.serverUuid = serverUuid;
        this.state = state;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(serverUuid.toString());
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
            serverUuid = UUID.fromString(input.readUTF());
            state = State.valueOf(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
