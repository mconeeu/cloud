package eu.mcone.cloud.core.api;

import eu.mcone.cloud.core.api.server.Server;
import eu.mcone.cloud.core.api.template.Template;
import eu.mcone.cloud.core.api.wrapper.Wrapper;
import group.onegaming.networkmanager.host.api.module.NetworkModule;
import lombok.Getter;

import java.util.Collection;
import java.util.UUID;

public abstract class CloudMaster extends NetworkModule {

    @Getter
    private static CloudMaster instance;

    protected void setInstance(CloudMaster instance) {
        if (instance == null) {
            System.err.println("CloudMaster instance cannot be set twice!");
        } else {
            CloudMaster.instance = instance;
        }
    }

    public abstract Collection<Template> getTemplates();

    public abstract Template getTemplate(String name);


    public abstract Collection<Server> getServers();

    public abstract Server getServer(UUID uuid);

    public abstract Server getServer(String name);


    public abstract Collection<Wrapper> getWrappers();

    public abstract Wrapper getWrapper(UUID uuid);

}
