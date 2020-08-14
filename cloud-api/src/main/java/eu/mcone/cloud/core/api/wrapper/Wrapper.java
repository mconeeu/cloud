package eu.mcone.cloud.core.api.wrapper;

import eu.mcone.cloud.core.api.server.Server;

import java.util.Set;
import java.util.UUID;

public interface Wrapper {

    UUID getUuid();

    long getRam();

    long getRamInUse();

    boolean isBusy();

    Set<Server> getServers();

    void shutdown();

}
