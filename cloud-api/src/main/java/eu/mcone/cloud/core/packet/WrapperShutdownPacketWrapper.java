/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.packet;

import group.onegaming.networkmanager.api.packet.Packet;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;

@NoArgsConstructor
public class WrapperShutdownPacketWrapper extends Packet {

    @Override
    public void onWrite(DataOutputStream out) {}

    @Override
    public void onRead(DataInputStream in) {}

}
