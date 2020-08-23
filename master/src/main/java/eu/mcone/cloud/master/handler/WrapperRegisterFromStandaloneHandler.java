/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.WrapperRegisterFromStandalonePacketWrapper;
import eu.mcone.cloud.core.server.ServerRegisterData;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.BungeeServerListUpdater;
import eu.mcone.cloud.master.server.CloudServer;
import eu.mcone.cloud.master.wrapper.CloudWrapper;
import group.onegaming.networkmanager.api.packet.interfaces.PacketHandler;
import group.onegaming.networkmanager.core.api.console.ConsoleColor;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log
public class WrapperRegisterFromStandaloneHandler implements PacketHandler<WrapperRegisterFromStandalonePacketWrapper> {

    @Getter
    static final Map<UUID, Map<UUID, ServerRegisterData>> registeringServers = new HashMap<>();

    @Override
    public void onPacketReceive(WrapperRegisterFromStandalonePacketWrapper packet, ChannelHandlerContext chc) {
        log.fine("new WrapperRegisterFromStandalonePacketWrapper (UUID: " + packet.getUuid() + ", RAM: " + packet.getRam() + ", SERVERS: " + packet.getServers().toString() + ")");
        log.info("WrapperRegister - Wrapper registering with still " + packet.getServers().size() + " servers running!");

        List<UUID> unknown = new ArrayList<>();
        Map<UUID, CloudServer> newServers = new HashMap<>();
        for (HashMap.Entry<UUID, String> e : packet.getServers().entrySet()) {
            UUID uuid = e.getKey();
            String name = e.getValue();
            CloudServer s = (CloudServer) MasterServer.getServer().getServer(name);

            if (s == null || (s.getInfo().isStaticServer() && !s.getWrapperUuid().equals(packet.getUuid()))) {
                unknown.add(uuid);
            } else {
                s.setPreventStart(true);
                newServers.put(uuid, s);
            }
        }

        final CloudWrapper w = MasterServer.getServer().createWrapper(packet.getUuid(), chc.channel(), packet.getRam());

        log.info("WrapperRegister - Found " + newServers.size() + " valid servers!");
        log.info("WrapperRegister - Found " + unknown.size() + " invalid servers! Deleting from Wrapper...");
        for (UUID uuid : unknown) {
            w.deleteServer(uuid);
        }

        log.info("WrapperRegister - " + ConsoleColor.YELLOW + "Waiting 5sec for servers to register...");
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (registeringServers.containsKey(w.getUuid())) {
                final Map<UUID, ServerRegisterData> registeringServers = WrapperRegisterFromStandaloneHandler.registeringServers.get(w.getUuid());

                for (HashMap.Entry<UUID, CloudServer> e : newServers.entrySet()) {
                    final UUID uuid = e.getKey();
                    final CloudServer s = e.getValue();

                    s.getInfo().setUuid(uuid);
                    if (s.getState().equals(ServerState.OFFLINE)) s.setState(ServerState.BROKEN);

                    if (registeringServers.containsKey(uuid)) {
                        if (s.getInfo().isStaticServer() == registeringServers.get(uuid).getPacket().isStaticServer()
                                && registeringServers.get(uuid).getPacket().getVersion().equals(s.getInfo().getVersion())
                        ) {
                            log.info("WrapperRegister - " + ConsoleColor.GREEN + "Post-registering Server " + s.getInfo().getName() + "...");
                            s.registerFromPluginData(registeringServers.get(uuid));
                            registeringServers.remove(uuid);
                            w.createServer(s);

                            if (!s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                                BungeeServerListUpdater.registerServerOnAllBungees(s);
                            }
                        } else {
                            log.info("WrapperRegister - " + ConsoleColor.GREEN + "Post-reconfiguring Server " + s.getInfo().getName() + " as important variables were changed...");

                            try {
                                w.deleteServer(s.getInfo().getUuid()).await();
                                w.createServer(s).await();
                                s.start();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } else {
                        log.info("WrapperRegister - " + ConsoleColor.YELLOW + "Server " + s.getInfo().getName() + " is broken. Restarting...");
                        w.createServer(s);
                        s.restart();
                    }

                    s.setPreventStart(false);
                }
            } else {
                for (Map.Entry<UUID, CloudServer> e : newServers.entrySet()) {
                    final CloudServer s = e.getValue();
                    s.getInfo().setUuid(e.getKey());

                    if (s.getState().equals(ServerState.OFFLINE)) s.setState(ServerState.BROKEN);

                    log.info("WrapperRegister - " + ConsoleColor.YELLOW + "Server " + s.getInfo().getName() + " is broken. Restarting...");
                    w.createServer(s);
                    s.restart();
                    s.setPreventStart(false);
                }
            }

            registeringServers.remove(w.getUuid());
            log.info("WrapperRegister - " + ConsoleColor.GREEN + "Wrapper " + w.getUuid() + " is successfully registered from Standalone Mode!");
        }, 5000, TimeUnit.MILLISECONDS);
    }

    static void addNonExistingRegisteringServer(UUID serverUuid, ServerRegisterData data) {
        if (registeringServers.containsKey(data.getPacket().getWrapperUuid())) {
            registeringServers.get(data.getPacket().getWrapperUuid()).put(serverUuid, data);
        } else {
            registeringServers.put(data.getPacket().getWrapperUuid(), new HashMap<UUID, ServerRegisterData>() {{
                put(serverUuid, data);
            }});
        }
    }

}
