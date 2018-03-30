/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.packet.*;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.core.server.ServerVersion;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.request.GetRequest;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.wrapper.Wrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.*;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private Map<UUID, FutureTask<Packet>> tasks;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        tasks = new HashMap<>();
        Logger.log(getClass(), "new channel from " + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof WrapperRegisterPacketWrapper) {
            WrapperRegisterPacketWrapper result = (WrapperRegisterPacketWrapper) packet;
            Logger.log(getClass(), "new WrapperRegisterPacketWrapper (UUID: "+result.getUuid()+", RAM: "+result.getRam()+")");

            MasterServer.getInstance().createWrapper(result.getUuid(), ctx.channel(), result.getRam());
        } else if (packet instanceof WrapperRegisterFromStandalonePacketWrapper) {
            WrapperRegisterFromStandalonePacketWrapper result = (WrapperRegisterFromStandalonePacketWrapper) packet;
            Logger.log(getClass(), "new WrapperRegisterFromStandalonePacketWrapper (UUID: "+result.getUuid()+", RAM: "+result.getRam()+", SERVERS: "+result.getServers().toString()+")");
            Logger.log("WrapperRegister", "Wrapper registering with still "+result.getServers().size()+" servers running!");

            List<UUID> unknown = new ArrayList<>();
            List<Server> newServer = new ArrayList<>();
            for (HashMap.Entry<UUID, String> e : result.getServers().entrySet()) {
                UUID uuid = e.getKey();
                String name = e.getValue();
                Server s = MasterServer.getInstance().getServer(name);

                if (s == null) {
                    unknown.add(uuid);
                } else {
                    s.getInfo().setUuid(uuid);
                    newServer.add(s);
                }
            }

            Wrapper w = MasterServer.getInstance().createWrapper(result.getUuid(), ctx.channel(), result.getRam());

            Logger.log("WrapperRegister", "Found "+newServer.size()+" valid servers!");
            for (Server s : newServer) {
                w.createServer(s);
            }

            Logger.log("WrapperRegister", "Found "+unknown.size()+" invalid servers! Deleting from Wrapper...");
            for (UUID uuid : unknown) {
                w.destroyServer(uuid);
            }

            Logger.log("WrapperRegister", w.getUuid()+" is successfully registered from Standalone Mode!");
        } else if (packet instanceof ServerRegisterPacketPlugin) {
            ServerRegisterPacketPlugin result = (ServerRegisterPacketPlugin) packet;
            Logger.log(getClass(), "new ServerRegisterPacketPlugin (UUID: "+result.getServerUuid()+", PORT: "+result.getPort()+")");
            Server s = MasterServer.getInstance().getServer(result.getServerUuid());

            if (s != null) {
                s.setChannel(ctx.channel());
                s.setState(result.getState());
                s.getInfo().setHostname(result.getHostname());
                s.getInfo().setPort(result.getPort());

                if (s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                    for (Server server : MasterServer.getInstance().getServers()) {
                        if (!server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                            s.send(new ServerListUpdatePacketPlugin(server.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
                        }
                    }
                }
            }
        } else if (packet instanceof ServerPlayerCountUpdatePacketPlugin) {
            ServerPlayerCountUpdatePacketPlugin result = (ServerPlayerCountUpdatePacketPlugin) packet;
            Logger.log(getClass(), "new ServerPlayerCountUpdatePacketPlugin (UUID: "+result.getUuid()+", COUNT: "+result.getPlayerCount()+")");
            Server s = MasterServer.getInstance().getServer(result.getUuid());

            if (s != null) {
                s.setPlayerCount(result.getPlayerCount());
            } else {
                Logger.err(getClass(), "Playercount for Server with UUID "+result.getUuid()+" could not be changed! (Server does not exist)");
            }
        } else if (packet instanceof ServerUpdateStatePacket) {
            ServerUpdateStatePacket result = (ServerUpdateStatePacket) packet;
            Logger.log(getClass(), "new ServerUpdateStatePacketPlugin (UUID: "+result.getUuid()+", STATE: "+result.getState().toString()+")");
            ServerState state = result.getState();
            Server s = MasterServer.getInstance().getServer(result.getUuid());

            if (s != null) {
                s.setState(state);

                boolean busy = false;
                for (Server ser : s.getWrapper().getServers()) {
                    if (busy) break;
                    busy = ser.getState().equals(ServerState.STARTING);
                }
                Logger.log(getClass(), "["+s.getWrapper().getUuid()+"] Wrapper is busy? "+busy);
                s.getWrapper().setBusy(busy);

                if (!s.getInfo().getVersion().equals(ServerVersion.BUNGEE)) {
                    switch (state) {
                        case WAITING: {
                            for (Server server : MasterServer.getInstance().getServers()) {
                                if (server.getInfo().getVersion().equals(ServerVersion.BUNGEE) && !server.getState().equals(ServerState.OFFLINE)) {
                                    System.out.println("registering Server "+s.getInfo().getName()+ " @ Bungee "+server.getInfo().getName());
                                    server.send(new ServerListUpdatePacketPlugin(s.getInfo(), ServerListUpdatePacketPlugin.Scope.ADD));
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
            Logger.log(getClass(), "[" + result.getResultClass() + "] " + result.getMessage() + " ResultType: " + result.getResult());
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
            w.delete();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            ctx.channel().close();

            Logger.err(getClass(), "Lost Connection to Client: "+ctx.channel().remoteAddress().toString()+".");
            Logger.err(getClass(), cause.getMessage());
            return;
        }

        Logger.err(getClass(), "Netty Exception:");
        cause.printStackTrace();
    }

}
