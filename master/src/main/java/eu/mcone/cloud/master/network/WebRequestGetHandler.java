/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.java.Log;

@Log
public class WebRequestGetHandler implements eu.mcone.networkmanager.api.network.client.handler.WebRequestGetHandler {

    @Override
    public String onRequest(JsonElement jsonElement) {
        JsonObject jObject = jsonElement.getAsJsonObject();

        /*switch (jObject.get("request").getAsString()) {
            case "server": {
                UUID serverUuid = UUID.fromString(jObject.get("uuid").getAsString());
                Server s = MasterServer.getInstance().getServer(serverUuid);

                if (s != null) {
                    switch (jObject.get("value").getAsString()) {
                        case "log": {
                            return WrapperRequestUtil.request(s.getWrapper(), "log", packet -> {
                                ServerLogPacketClient result = (ServerLogPacketClient) packet;
                                JsonArray log = new JsonArray();

                                for (String line : result.getLog()) {
                                    log.add(line);
                                }

                                return MasterServer.getInstance().getGson().toJson(log);
                            });


                            tasks.put(request, ctx::writeAndFlush);
                            s.getWrapper().send(new WrapperRequestPacketMaster(WrapperRequestPacketMaster.Type.LOG, request, serverUuid.toString()));
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


        case "SET":
        case "set": {
            if (jObject.get("request").getAsString().equalsIgnoreCase("server")) {
                Server s = MasterServer.getInstance().getServer(UUID.fromString(jObject.get("uuid").getAsString()));

                if (s != null) {
                    switch (jObject.get("action").getAsString()) {
                        case "start":
                            s.start();
                            break;
                        case "stop":
                            s.stop();
                            break;
                        case "forcestop":
                            s.forcestop();
                            break;
                        case "restart":
                            s.restart();
                            break;
                    }
                }
            }
            break;
        }*/
        return null;
    }

}
