/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ServerLogPacketClient extends WrapperRequestPacketMaster {

    private List<String> log;

    public ServerLogPacketClient(UUID uuid, List<String> log) {
        super(uuid, null);
        this.log = log;
    }

    @Override
    public void writeRequest(DataOutputStream out) throws IOException {
        out.writeInt(log.size());

        for (String line : log) {
            out.writeUTF(line);
        }
    }

    @Override
    public void readRequest(DataInputStream in) throws IOException {
        log = new ArrayList<>();

        for (int i = 0; i < in.readInt(); i++) {
            log.add(in.readUTF());
        }
    }

}
