/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server.console;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.server.ServerState;
import eu.mcone.cloud.wrapper.WrapperServer;
import eu.mcone.cloud.wrapper.server.Server;

public class BungeeInputReader extends ConsoleInputReader {

    public BungeeInputReader(Server server, Boolean outputToConsole) {
        super(server, outputToConsole);
    }

    @Override
    void filter(String[] lineArray, String line) {
        if (line.contains("Listening on ")) {
            //Server started
            this.server.setState(ServerState.WAITING);
            Logger.log(getClass(), "[" + this.server.getInfo().getName() + "] Server successfully started!");
        }
    }
}
