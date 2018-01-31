package eu.mcone.cloud.plugin;

import eu.mcone.cloud.plugin.network.ClientBootstrap;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

public class CloudPlugin {

    @Getter @Setter
    private Channel channel;
    @Getter
    private String serverName;
    @Getter
    private UUID serverUuid;
    @Getter
    private int serverPort;

    public CloudPlugin() {
        Properties ps = new Properties();
        try {
            ps.load(new InputStreamReader(Files.newInputStream(Paths.get("server.properties"))));

            serverName = ps.getProperty("server-name");
            serverUuid = UUID.fromString(ps.getProperty("server-uuid"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverPort = Bukkit.getServer().getPort();

        new ClientBootstrap("localhost", 4567, this);
    }

    public void unload() {

    }

}
