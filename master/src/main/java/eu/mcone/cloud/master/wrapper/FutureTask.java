/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.packet.WrapperRequestPacketMaster;

public interface FutureTask {

    Object onReplyReceive(WrapperRequestPacketMaster packet);

}
