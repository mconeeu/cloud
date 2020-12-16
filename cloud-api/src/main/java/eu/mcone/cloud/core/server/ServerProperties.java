/*
 * Copyright (c) 2017 - 2021 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.core.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter @Setter
public class ServerProperties {

    private List<String> plugins;
    private List<String> worlds;
    private List<Config> configs;
    private String gamemode, mode;

    @AllArgsConstructor
    @Getter @Setter
    public static class Config {
        private String name;
        private Map<String, Object> values;
    }

}
