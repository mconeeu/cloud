/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
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
public class PluginRegisterData {

    private Channel channel;
    private ServerRegisterPacketPlugin packet;

}
