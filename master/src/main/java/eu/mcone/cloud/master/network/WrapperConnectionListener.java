package eu.mcone.cloud.master.network;

import eu.mcone.cloud.master.MasterServer;
import eu.mcone.cloud.master.wrapper.Wrapper;
import eu.mcone.networkmanager.api.server.ConnectionListener;
import eu.mcone.networkmanager.core.api.console.ConsoleColor;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.java.Log;

@Log
public class WrapperConnectionListener implements ConnectionListener {

    @Override
    public void onChannelActive(ChannelHandlerContext channelHandlerContext) {}

    @Override
    public void onChannelUnregistered(ChannelHandlerContext ctx) {
        Wrapper w = MasterServer.getInstance().getWrapper(ctx.channel());
        if (w != null) {
            log.info(ConsoleColor.RED+"Deleting Wrapper "+w.getUuid());
            w.delete();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {}

}
