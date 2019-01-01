/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.networkmanager.api.network.packet.Packet;
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

    private UUID uuid;
    private int playerCount;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
            out.writeUTF(uuid.toString());
            out.writeInt(playerCount);
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
            uuid = UUID.fromString(in.readUTF());
            playerCount = in.readInt();
    }
}
