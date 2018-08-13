/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.server.Server;
import lombok.extern.java.Log;

@Log
public class BungeeInputReader extends ConsoleInputReader {

    public BungeeInputReader(Server server, Boolean outputToConsole) {
        super(server, outputToConsole);
    }

    @Override
    void filter(String line) {
        if (line.contains("Listening on ") || line.contains(" Could not bind to host /")) {
            //Server started
            this.server.setState(ServerState.WAITING);
            log.info("[" + this.server.getInfo().getName() + "] Server successfully started!");
        }
    }
}
