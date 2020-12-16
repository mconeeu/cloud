/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import group.onegaming.networkmanager.api.packet.ClientMessageRequestPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudServerForwardedRequestPacket extends ClientMessageRequestPacket {

    private UUID fromServer, toServer;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
        out.writeUTF(fromServer.toString());
        out.writeUTF(toServer.toString());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
        fromServer = UUID.fromString(in.readUTF());
        toServer = UUID.fromString(in.readUTF());
    }

}
