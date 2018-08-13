/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.PluginRegisterData;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.request.GetRequest;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log
public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private static Map<UUID, FutureTask<Packet>> tasks = new HashMap<>();
    @Getter
    private static Map<UUID, PluginRegisterData> registeringServers = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("new channel from " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof WrapperRegisterPacketWrapper) {
            WrapperRegisterPacketWrapper result = (WrapperRegisterPacketWrapper) packet;
            log.fine("new WrapperRegisterPacketWrapper (UUID: "+result.getUuid()+", RAM: "+result.getRam()+")");

            MasterServer.getInstance().createWrapper(result.getUuid(), ctx.channel(), result.getRam());
        } else if (packet instanceof WrapperRegisterFromStandalonePacketWrapper) {
            WrapperRegisterFromStandalonePacketWrapper result = (WrapperRegisterFromStandalonePacketWrapper) packet;
            log.fine("new WrapperRegisterFromStandalonePacketWrapper (UUID: "+result.getUuid()+", RAM: "+result.getRam()+", SERVERS: "+result.getServers().toString()+")");
            log.info("WrapperRegister - Wrapper registering with still "+result.getServers().size()+" servers running!");

            List<UUID> unknown = new ArrayList<>();
            Map<UUID, Server> newServers = new HashMap<>();
            for (HashMap.Entry<UUID, String> e : result.getServers().entrySet()) {
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

            final Wrapper w = MasterServer.getInstance().createWrapper(result.getUuid(), ctx.channel(), result.getRam());

            log.info("WrapperRegister - Found "+newServers.size()+" valid servers!");
            log.info("WrapperRegister - Found "+unknown.size()+" invalid servers! Deleting from Wrapper...");
            for (UUID uuid : unknown) {
                w.destroyServer(uuid);
            }

            log.info("WrapperRegister - "+ConsoleColor.YELLOW+"Waiting 5sec for servers to register...");
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                final Map<UUID, PluginRegisterData> waiting = ChannelPacketHandler.getRegisteringServers();
                for (HashMap.Entry<UUID, Server> e : newServers.entrySet()) {
                    final UUID uuid = e.getKey();
                    final Server s = e.getValue();

                    s.getInfo().setUuid(uuid);

                    if (s.getState().equals(ServerState.OFFLINE)) s.setState(ServerState.BROKEN);
                    if (waiting.containsKey(uuid)) {
                        log.info("WrapperRegister - "+ConsoleColor.GREEN+"Post-registering Server "+s.getInfo().getName()+"...");
                        s.registerPluginData(waiting.get(uuid));
                        ChannelPacketHandler.registeringServers.remove(uuid);
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
        } else if (packet instanceof ServerRegisterPacketPlugin) {
            ServerRegisterPacketPlugin result = (ServerRegisterPacketPlugin) packet;
            log.fine("new ServerRegisterPacketPlugin (UUID: "+result.getServerUuid()+", PORT: "+result.getPort()+")");
            Server s = MasterServer.getInstance().getServer(result.getServerUuid());

            if (s != null) {
                s.registerPluginData(new PluginRegisterData(ctx.channel(), result));
            } else {
                try {
                    registeringServers.put(result.getServerUuid(), new PluginRegisterData(ctx.channel(), result));
                    log.info("Server with uuid " + result.getServerUuid() + " tried to register itself from " + result.getHostname() + " but the server is not known! Put in waitlist.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (packet instanceof ServerPlayerCountUpdatePacketPlugin) {
            ServerPlayerCountUpdatePacketPlugin result = (ServerPlayerCountUpdatePacketPlugin) packet;
            log.fine("new ServerPlayerCountUpdatePacketPlugin (UUID: "+result.getUuid()+", COUNT: "+result.getPlayerCount()+")");
            Server s = MasterServer.getInstance().getServer(result.getUuid());

            if (s != null) {
                s.setPlayerCount(result.getPlayerCount());
            } else {
                log.warning("Playercount for Server with UUID "+result.getUuid()+" could not be changed! (Server does not exist)");
            }
        } else if (packet instanceof ServerUpdateStatePacket) {
            ServerUpdateStatePacket result = (ServerUpdateStatePacket) packet;
            log.fine("new ServerUpdateStatePacketPlugin (UUID: "+result.getUuid()+", STATE: "+result.getState().toString()+")");
            ServerState state = result.getState();
            Server s = MasterServer.getInstance().getServer(result.getUuid());

            if (s != null) {
                s.setState(state);

                boolean busy = false;
                for (Server ser : s.getWrapper().getServers()) {
                    if (busy) break;
                    busy = ser.getState().equals(ServerState.STARTING);
                }
                log.info("["+s.getWrapper().getUuid()+"] Wrapper is busy? "+busy);
                s.getWrapper().setBusy(busy);

                if (!s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                    switch (state) {
                        case WAITING: {
                            for (Server server : MasterServer.getInstance().getServers()) {
                                if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                    server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
                                    System.out.println("registered Server "+s.getInfo().getName()+ " @ Bungee "+server.getInfo().getName());
                                }
                            }
                            break;
                        }
                        case OFFLINE: {
                            for (Server server : MasterServer.getInstance().getServers()) {
                                if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                    System.out.println("deleting Server "+s.getInfo().getName()+ " @ Bungee "+server.getInfo().getName());
                                    server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.REMOVE));
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } else if (packet instanceof ServerResultPacketWrapper) {
            ServerResultPacketWrapper result = (ServerResultPacketWrapper) packet;
            log.fine("[" + result.getResultClass() + "] " + result.getMessage() + " ResultType: " + result.getResult());
        } else if (packet instanceof MasterRequestPacketClient) {
            MasterRequestPacketClient result = (MasterRequestPacketClient) packet;
            JsonObject jObject = new JsonParser().parse(result.getJson()).getAsJsonObject();

            switch (jObject.get("method").getAsString()) {
                case "GET":
                case "get": {
                    switch (jObject.get("request").getAsString()) {
                        case "server": {
                            UUID serverUuid = UUID.fromString(jObject.get("uuid").getAsString());
                            Server s = MasterServer.getInstance().getServer(serverUuid);

                            if (s != null) {
                                switch (jObject.get("value").getAsString()) {
                                    case "log": {
                                        UUID request = UUID.randomUUID();
                                        tasks.put(request, ctx::writeAndFlush);
                                        s.getWrapper().send(new WrapperRequestPacketMaster(WrapperRequestPacketMaster.Object.LOG, request, serverUuid.toString()));
                                        break;
                                    }
                                    case "state": {
                                        String json = new GetRequest(s).log();
                                        ctx.writeAndFlush(new ClientReturnPacketMaster(UUID.randomUUID(), json));
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case "all": {
                            String json = new GetRequest().all();
                            ctx.writeAndFlush(new ClientReturnPacketMaster(UUID.randomUUID(), json));
                            break;
                        }
                    }
                    break;
                }
                case "SET":
                case "set": {
                    if (jObject.get("request").getAsString().equalsIgnoreCase("server")) {
                        Server s = MasterServer.getInstance().getServer(UUID.fromString(jObject.get("uuid").getAsString()));

                        if (s != null) {
                            switch (jObject.get("action").getAsString()) {
                                case "start" : s.start(); break;
                                case "stop" : s.stop(); break;
                                case "forcestop" : s.forcestop(); break;
                                case "restart" : s.restart(); break;
                            }
                        }
                    }
                    break;
                }
            }
        } else if (packet instanceof ClientReturnPacketMaster) {
            ClientReturnPacketMaster result = (ClientReturnPacketMaster) packet;
            FutureTask<Packet> task = tasks.get(result.getRequest());

            if (task != null) {
                task.run(packet);
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Wrapper w = MasterServer.getInstance().getWrapper(ctx.channel());
        if (w != null) {
            log.info(ConsoleColor.RED+"Deleting Wrapper "+w.getUuid());
            w.delete();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            ctx.channel().close();

            log.warning("Lost Connection to Client: "+ctx.channel().remoteAddress().toString()+".");
            log.warning(cause.getMessage());
            return;
        }

        log.severe("Netty Exception:");
        cause.printStackTrace();
    }

}
