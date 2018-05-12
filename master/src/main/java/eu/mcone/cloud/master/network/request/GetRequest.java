/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network.request;

import com.google.gson.JsonObject;
import eu.mcone.cloud.core.api.CloudMaster;
import eu.mcone.cloud.core.api.CloudServer;
import eu.mcone.cloud.core.api.CloudTemplate;
import eu.mcone.cloud.core.api.CloudWrapper;
import eu.mcone.cloud.master.MasterServer;
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
        List<CloudTemplate> templates = new ArrayList<>();
        List<CloudServer> servers = new ArrayList<>();
        List<CloudWrapper> wrappers = new ArrayList<>();

        for (Template t : MasterServer.getInstance().getTemplates()) {
            List<String> tServers = new ArrayList<>();

            for (Server ser : t.getServers()) {
                servers.add(new CloudServer(ser.getInfo().getName(), ser.getInfo().getTemplateName(), ser.getWrapper().getUuid().toString(), ser.getChannel().toString(), ser.getState().toString(), ser.getInfo().getVersion().toString(), ser.getInfo().getPort(), ser.getPlayerCount(), ser.getInfo().getMaxPlayers(), ser.getInfo().getRam()));
                tServers.add(ser.getInfo().getUuid().toString());
            }

            templates.add(new CloudTemplate(t.getName(), t.getServers().size(), t.getMaxPlayers(), t.getMin(), t.getMax(), t.getEmptyservers(), tServers));
        }

        for (Wrapper w : MasterServer.getInstance().getWrappers()) {
            List<String> wServers = new ArrayList<>();

            for (Server ser : w.getServers()) {
                wServers.add(ser.getInfo().getUuid().toString());
            }

            wrappers.add(new CloudWrapper(w.getUuid().toString(), w.getChannel().toString(), w.getRam(), w.getRamInUse(), w.isBusy(), wServers));
        }

        return MasterServer.getInstance().getGson().toJson(new CloudMaster(templates, wrappers, servers));
    }

    public String log() {
        JsonObject jObject = new JsonObject();
        jObject.addProperty("uuid", server.getInfo().getUuid().toString());
        jObject.addProperty("state", server.getState().toString());

        return MasterServer.getInstance().getGson().toJson(jObject);
    }

}
