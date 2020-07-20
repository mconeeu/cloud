/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
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
public class ServerRegisterPacketPlugin extends Packet {

    private UUID serverUuid, wrapperUuid;
    private String hostname;
    private int port, playercount;
    private ServerState state;
    private ServerVersion version;
    private boolean staticServer;

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        out.writeUTF(serverUuid.toString());
        out.writeUTF(wrapperUuid.toString());
        out.writeUTF(hostname);
        out.writeInt(port);
        out.writeInt(playercount);
        out.writeUTF(state.toString());
        out.writeUTF(version.toString());
        out.writeBoolean(staticServer);
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        serverUuid = UUID.fromString(in.readUTF());
        wrapperUuid = UUID.fromString(in.readUTF());
        hostname = in.readUTF();
        port = in.readInt();
        playercount = in.readInt();
        state = ServerState.valueOf(in.readUTF());
        version = ServerVersion.valueOf(in.readUTF());
        staticServer = in.readBoolean();
    }

}
