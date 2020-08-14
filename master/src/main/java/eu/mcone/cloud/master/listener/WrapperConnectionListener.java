package eu.mcone.cloud.master.listener;

import eu.mcone.cloud.core.api.wrapper.Wrapper;
import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.wrapper.CloudWrapper;
import group.onegaming.networkmanager.core.api.console.ConsoleColor;
import group.onegaming.networkmanager.host.api.server.ConnectionListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperConnectionListener implements ConnectionListener {

    @Override
    public void onChannelActive(String resourceBundleName, ChannelHandlerContext channelHandlerContext) {}

    @Override
    public void onChannelUnregistered(ChannelHandlerContext ctx) {
        Wrapper w = MasterServer.getServer().getWrapper(ctx.channel());
        if (w != null) {
            log.info(ConsoleColor.RED+"Deleting Wrapper "+w.getUuid());
            ((CloudWrapper) w).unregister();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {}

}
