/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import group.onegaming.networkmanager.api.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServerPlayerCountUpdatePacketPlugin extends Packet {

    public enum Method {
        ADD, REMOVE
    }

    private UUID serverUuid;
    private UUID uuid;
    private String name;
    private Method method;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
            out.writeUTF(serverUuid.toString());
            out.writeUTF(uuid.toString());
            out.writeUTF(name);
            out.writeUTF(method.toString());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
            serverUuid = UUID.fromString(in.readUTF());
            uuid = UUID.fromString(in.readUTF());
            name = in.readUTF();
            method = Method.valueOf(in.readUTF());
    }

}
