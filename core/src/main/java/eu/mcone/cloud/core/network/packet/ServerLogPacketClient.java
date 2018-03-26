/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.network.packet;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerLogPacketClient extends ClientReturnPacketMaster {

    private UUID serverUuid;
    private List<String> log;

    public ServerLogPacketClient() {}

    public ServerLogPacketClient(UUID request, UUID serverUuid, List<String> log) {
        super(request, null);

        this.serverUuid = serverUuid;
        this.log = log;
    }

    @Override
    public void write(ByteBuf byteBuf) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(request.toString());
            out.writeUTF(serverUuid.toString());

            out.writeInt(log.size());
            for (String line : log) {
                out.writeUTF(line);
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
            request = UUID.fromString(input.readUTF());
            serverUuid = UUID.fromString(input.readUTF());

            int size = input.readInt();
            log = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                log.add(input.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
