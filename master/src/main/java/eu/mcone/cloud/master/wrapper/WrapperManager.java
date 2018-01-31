/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.wrapper;

import eu.mcone.cloud.master.MasterServer;

import java.util.HashMap;
import java.util.List;

public class WrapperManager {

    public static Wrapper getWrapperbyString(String wrapperName) {
        for (Wrapper w : MasterServer.getInstance().getWrappers()) {
            if (w.getName().equalsIgnoreCase(wrapperName)) {
                return w;
            }
        }

        return null;
    }

}
