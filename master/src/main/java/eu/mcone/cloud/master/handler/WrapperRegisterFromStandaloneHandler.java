/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.handler;

import eu.mcone.cloud.core.packet.WrapperRegisterFromStandalonePacketWrapper;
import eu.mcone.cloud.core.server.PluginRegisterData;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.api.network.client.handler.PacketHandler;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log
public class WrapperRegisterFromStandaloneHandler implements PacketHandler<WrapperRegisterFromStandalonePacketWrapper> {

    @Getter
    static Map<UUID, PluginRegisterData> registeringServers = new HashMap<>();

    @Override
    public void onPacketReceive(WrapperRegisterFromStandalonePacketWrapper packet, ChannelHandlerContext chc) {
        log.fine("new WrapperRegisterFromStandalonePacketWrapper (UUID: "+packet.getUuid()+", RAM: "+packet.getRam()+", SERVERS: "+packet.getServers().toString()+")");
        log.info("WrapperRegister - Wrapper registering with still "+packet.getServers().size()+" servers running!");

        List<UUID> unknown = new ArrayList<>();
        Map<UUID, Server> newServers = new HashMap<>();
        for (HashMap.Entry<UUID, String> e : packet.getServers().entrySet()) {
            UUID uuid = e.getKey();
            String name = e.getValue();
            Server s = MasterServer.getInstance().getServer(name);

            if (s == null) {
                unknown.add(uuid);
            } else {
                s.setPreventStart(true);
                newServers.put(uuid, s);
            }
        }

        final Wrapper w = MasterServer.getInstance().createWrapper(packet.getUuid(), chc.channel(), packet.getRam());

        log.info("WrapperRegister - Found "+newServers.size()+" valid servers!");
        log.info("WrapperRegister - Found "+unknown.size()+" invalid servers! Deleting from Wrapper...");
        for (UUID uuid : unknown) {
            w.destroyServer(uuid);
        }

        log.info("WrapperRegister - "+ ConsoleColor.YELLOW+"Waiting 5sec for servers to register...");
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            for (HashMap.Entry<UUID, Server> e : newServers.entrySet()) {
                final UUID uuid = e.getKey();
                final Server s = e.getValue();

                s.getInfo().setUuid(uuid);

                if (s.getState().equals(ServerState.OFFLINE)) s.setState(ServerState.BROKEN);
                if (registeringServers.containsKey(uuid)) {
                    log.info("WrapperRegister - "+ConsoleColor.GREEN+"Post-registering Server "+s.getInfo().getName()+"...");
                    s.registerPluginData(registeringServers.get(uuid));
                    registeringServers.remove(uuid);
                    w.createServer(s);
                } else {
                    log.info("WrapperRegister - "+ConsoleColor.YELLOW+"Server "+s.getInfo().getName()+" is broken. Restarting...");
                    w.createServer(s);
                    s.restart();
                }

                s.setPreventStart(false);
            }
            log.info("WrapperRegister - "+ConsoleColor.GREEN+"Wrapper "+w.getUuid()+" is successfully registered from Standalone Mode!");
        }, 5000, TimeUnit.MILLISECONDS);
    }

}