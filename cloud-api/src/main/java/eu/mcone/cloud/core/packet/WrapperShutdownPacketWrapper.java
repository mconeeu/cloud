/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import eu.mcone.networkmanager.api.network.packet.Packet;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class WrapperShutdownPacketWrapper extends Packet {

    @Override
    public void onWrite(DataOutputStream out) {}

    @Override
    public void onRead(DataInputStream in) {}

}
