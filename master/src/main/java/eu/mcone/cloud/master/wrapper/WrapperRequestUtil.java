/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.core.packet.WrapperRequestPacketMaster;
import eu.mcone.networkmanager.api.network.client.PacketHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WrapperRequestUtil implements PacketHandler<WrapperRequestPacketMaster> {

    private static Map<UUID, FutureTask> tasks = new HashMap<>();

    public static void request(Wrapper wrapper, String request, FutureTask task) {
        UUID uuid = UUID.randomUUID();

        tasks.put(uuid, task);
        wrapper.send(new WrapperRequestPacketMaster(uuid, request));
    }

    @Override
    public void onPacketReceive(WrapperRequestPacketMaster packet, ChannelHandlerContext chc) {
        if (tasks.containsKey(packet.getUuid())) {
            tasks.get(packet.getUuid()).onReplyReceive(packet);
        }
    }

}
