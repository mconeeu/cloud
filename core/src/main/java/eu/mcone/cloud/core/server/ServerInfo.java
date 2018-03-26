/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ServerInfo {

    @Getter @Setter
    private UUID uuid;
    @Getter @Setter
    private String name, templateName, hostname;
    @Getter @Setter
    private int templateID, maxPlayers, port = 0;
    @Getter @Setter
    private long ram;
    @Getter @Setter
    private boolean staticServer;
    @Getter @Setter
    private ServerVersion version;

    public ServerInfo(UUID uuid, String name, String templateName, int maxPlayers, int templateID, long ram, boolean staticServer, ServerVersion version) {
        this.uuid = uuid;
        this.name = name;
        this.templateName = templateName;
        this.maxPlayers = maxPlayers;
        this.ram = ram;
        this.templateID = templateID;
        this.staticServer = staticServer;
        this.version = version;
    }

}
