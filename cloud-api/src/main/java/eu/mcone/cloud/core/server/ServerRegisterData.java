/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import eu.mcone.cloud.core.packet.ServerRegisterPacketPlugin;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class ServerRegisterData {

    private Channel channel;
    private ServerRegisterPacketPlugin packet;

}
