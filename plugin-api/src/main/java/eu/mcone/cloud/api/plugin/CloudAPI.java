/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.api.plugin;

import eu.mcone.cloud.core.packet.CloudInfoResponsePacket;
import eu.mcone.cloud.core.server.CloudWorld;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import group.onegaming.networkmanager.api.packet.Packet;
import io.netty.channel.ChannelFuture;
import eu.mcone.networkmanager.api.messaging.request.ClientMessageRequestListener;
import eu.mcone.networkmanager.api.messaging.response.CustomClientMessageResponseListener;
import eu.mcone.networkmanager.client.api.PacketManager;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public abstract class CloudAPI {

    @Getter
    private static CloudAPI instance;

    public CloudAPI() {}

    public void setInstance(CloudAPI instance) {
        if (instance == null) {
            System.err.println("CloudAPI instance cannot be set twice!");
        } else {
            CloudAPI.instance = instance;
        }
    }

    public abstract CloudPlugin getPlugin();

    public abstract String getServerName();

    public abstract String getHostname();

    public abstract ServerState getState();

    public abstract ServerVersion getVersion();

    public abstract boolean isStaticServer();

    public abstract List<CloudWorld> getLoadedWorlds();

    public abstract UUID getServerUuid();

    public abstract UUID getWrapperUuid();

    public abstract int getPort();

    public abstract PacketManager getPacketManager();

    public abstract void registerClientMessageListener(String uri, ClientMessageRequestListener listener) throws IllegalStateException;

    public abstract void getCloudInfo(CustomClientMessageResponseListener<CloudInfoResponsePacket> callback);

}
