/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;


import eu.mcone.networkmanager.api.packet.Packet;
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
public class ServerChangeStatePacketWrapper extends Packet {

    private UUID serverUuid;
    private State state;

    public enum State {
        START, STOP, FORCESTOP, RESTART, DELETE
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
            out.writeUTF(serverUuid.toString());
            out.writeUTF(state.toString());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
            serverUuid = UUID.fromString(in.readUTF());
            state = State.valueOf(in.readUTF());
    }

}
