package eu.mcone.cloud.plugin;

import eu.mcone.cloud.plugin.network.ClientBootstrap;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

public class CloudPlugin {

    @Getter @Setter
    private Channel channel;

    public CloudPlugin() {
        new ClientBootstrap("localhost", 4567, this);
    }

    public void unload() {

    }

}
