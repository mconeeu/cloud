package eu.mcone.cloud.master.network;

import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import eu.mcone.networkmanager.host.api.server.ConnectionListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperConnectionListener implements ConnectionListener {

    @Override
    public void onChannelActive(String s, ChannelHandlerContext channelHandlerContext) {}

    @Override
    public void onChannelUnregistered(ChannelHandlerContext ctx) {
        Wrapper w = MasterServer.getInstance().getWrapper(ctx.channel());
        if (w != null) {
            log.info(ConsoleColor.RED+"Deleting Wrapper "+w.getUuid());
            w.unregister();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {}

}
