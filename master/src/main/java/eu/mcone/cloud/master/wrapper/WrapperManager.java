/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.master.MasterServer;

import java.util.HashMap;

public class WrapperManager {

    public static Wrapper getWrapperbyString(String wrapperName) {
        HashMap<String, Wrapper> wrappers = MasterServer.wrappers;

        return wrappers.getOrDefault(wrapperName, null);
    }

}
