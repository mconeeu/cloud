package eu.mcone.cloud.core.server;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ServerInfo {

    @Getter @Setter
    private UUID uuid;

    @Getter @Setter
    private String name, templateName;

    @Getter @Setter
    private ServerState state;

    @Getter @Setter
    private int templateID, maxPlayers, ram, port = 0;

    public ServerInfo(UUID uuid, String name, String templateName, int maxPlayers, int templateID, int ram) {
        this.uuid = uuid;
        this.name = name;
        this.templateName = templateName;
        this.maxPlayers = maxPlayers;
        this.ram = ram;
        this.templateID = templateID;

        this.state = ServerState.STOPPED;
    }
}
