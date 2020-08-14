/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.server;

import com.google.common.io.Files;
import eu.mcone.cloud.core.server.ServerProperties;
import eu.mcone.cloud.wrapper.WrapperServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

class ConfigSetter {

    private File file;

    ConfigSetter(Server server, ServerProperties.Config config) throws IOException {
        this.file = new File(server.getServerDir() + File.separator + config.getName());

        switch (Files.getFileExtension(config.getName())) {
            case "yml": setYamlValues(config.getValues()); break;
            case "properties": setPropertiesValues(config.getValues()); break;
            case "json": setJsonValues(config.getValues());
        }
    }

    private void setYamlValues(Map<String, Object> values) throws IOException {
        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        values.forEach(configuration::set);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
    }

    private void setPropertiesValues(Map<String, Object> values) throws IOException {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(file);
        properties.load(fis);

        values.forEach((key, value) -> properties.setProperty(key, (String) value));
        FileOutputStream fos = new FileOutputStream(file);
        properties.store(fos, "MCONE-Wrapper");

        fos.close();
        fis.close();
    }

    private void setJsonValues(Map<String, Object> values) {
        try {
            FileUtils.writeStringToFile(file, WrapperServer.getInstance().getGson().toJson(values));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
