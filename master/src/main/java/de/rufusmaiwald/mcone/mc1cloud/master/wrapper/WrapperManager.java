/*
 * Copyright (c) 2017 Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved.
 * You are not allowed to decompile the code.
 */

package de.rufusmaiwald.mcone.mc1cloud.master.wrapper;

import de.rufusmaiwald.mcone.mc1cloud.master.MasterServer;

import java.util.HashMap;

public class WrapperManager {

    public static Wrapper getWrapperbyString(String wrapperName) {
        HashMap<String, Wrapper> wrappers = MasterServer.wrappers;

        return wrappers.getOrDefault(wrapperName, null);
    }

}
