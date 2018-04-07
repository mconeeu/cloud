/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.network.request.pojo.Master;
import eu.mcone.cloud.master.server.Server;
import eu.mcone.cloud.master.template.Template;
import eu.mcone.cloud.master.wrapper.Wrapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GetRequest {

    @Getter
    private Server server;

    public GetRequest() {}

    public GetRequest(Server server) {
        this.server = server;
    }

    public String all() {
        List<eu.mcone.cloud.master.network.request.pojo.Template> templates = new ArrayList<>();
        List<eu.mcone.cloud.master.network.request.pojo.Server> servers = new ArrayList<>();
        List<eu.mcone.cloud.master.network.request.pojo.Wrapper> wrappers = new ArrayList<>();

        for (Template t : MasterServer.getInstance().getTemplates()) {
            List<String> tServers = new ArrayList<>();

            for (Server ser : t.getServers()) {
                servers.add(new eu.mcone.cloud.master.network.request.pojo.Server(ser.getInfo().getName(), ser.getInfo().getTemplateName(), ser.getWrapper().getUuid().toString(), ser.getChannel().toString(), ser.getState().toString(), ser.getInfo().getVersion().toString(), ser.getInfo().getPort(), ser.getPlayerCount(), ser.getInfo().getMaxPlayers(), ser.getInfo().getRam()));
                tServers.add(ser.getInfo().getUuid().toString());
            }

            templates.add(new eu.mcone.cloud.master.network.request.pojo.Template(t.getName(), t.getServers().size(), t.getMaxPlayers(), t.getMin(), t.getMax(), t.getEmptyservers(), tServers));
        }

        for (Wrapper w : MasterServer.getInstance().getWrappers()) {
            List<String> wServers = new ArrayList<>();

            for (Server ser : w.getServers()) {
                wServers.add(ser.getInfo().getUuid().toString());
            }

            wrappers.add(new eu.mcone.cloud.master.network.request.pojo.Wrapper(w.getUuid().toString(), w.getChannel().toString(), w.getRam(), w.getRamInUse(), w.isBusy(), wServers));
        }

        return new Gson().toJson(new Master(templates, wrappers, servers));
    }

    public String log() {
        JsonObject jObject = new JsonObject();
        jObject.addProperty("uuid", server.getInfo().getUuid().toString());
        jObject.addProperty("state", server.getState().toString());

        return new Gson().toJson(jObject);
    }

}
