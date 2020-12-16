/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.api.server;

import eu.mcone.cloud.core.api.wrapper.Wrapper;
import group.onegaming.networkmanager.api.packet.Packet;
import io.netty.channel.ChannelFuture;

public interface Server extends SimpleServer {

    Wrapper getWrapper();

    ChannelFuture start();

    ChannelFuture stop();

    ChannelFuture forcestop();

    ChannelFuture restart();

    void delete();

    ChannelFuture send(Packet packet);

}
