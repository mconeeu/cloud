/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import com.google.common.io.Files;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ConfigSetter {

    private File file;

    public ConfigSetter(Server server, ServerProperties.Config config) throws IOException {
        this.file = new File(server.getServerDir() + File.separator + config.getName());

        switch (Files.getFileExtension(config.getName())) {
            case "yml": setYamlValues(config.getValues()); break;
            case "properties": setPropertiesValues(config.getValues()); break;
        }
    }

    private void setYamlValues(Map<String, Object> values) throws IOException {
        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        values.forEach(configuration::set);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
    }

    private void setPropertiesValues(Map<String, Object> values) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        values.forEach((key, value) -> properties.setProperty(key, (String) value));
        properties.store(new FileOutputStream(file), "MCONE-Wrapper");
    }

}
