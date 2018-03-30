/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

import com.google.gson.Gson;
import eu.mcone.cloud.wrapper.server.ServerProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ServerPropertiesGsonTest {

    public static void main(String[] args) {
        String json = new Gson().toJson(
                new ServerProperties(
                        new ArrayList<>(Arrays.asList(
                                new ServerProperties.PluginDownload("test", "test", "test"),
                                new ServerProperties.PluginDownload("test2", "test2", "test2")
                        )),
                        new ArrayList<>(Arrays.asList(
                                "world1",
                                "world2"
                        )),
                        new ArrayList<>(Arrays.asList(
                                new ServerProperties.Config(
                                        "test.yml",
                                        new HashMap<String, Object>() {{
                                            put("hi", "hi");
                                            put("hi2", "hi2");
                                        }}
                                ),
                                new ServerProperties.Config(
                                        "test2.yml",
                                        new HashMap<String, Object>() {{
                                            put("hi", "hi");
                                            put("hi2", "hi2");
                                        }}
                                )
                        ))
                )
        );
        System.out.println(json);

        ServerProperties properties = new Gson().fromJson(json, ServerProperties.class);
        System.out.println(properties.getPlugins() + "\n" +
                properties.getWorlds() + "\n" +
                properties.getConfigs()
        );
    }

}
