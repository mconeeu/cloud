package eu.mcone.cloud.core.network;

import eu.mcone.cloud.core.network.packet.Packet;
import eu.mcone.cloud.core.network.packet.ServerInfoPacket;
import eu.mcone.cloud.core.server.ServerInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ChannelPacketHandler extends SimpleChannelInboundHandler<Packet> {

    @Getter
    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        System.out.println("new channel to / from "+ctx.channel().remoteAddress().toString());
        send(new ServerInfoPacket(new ServerInfo(UUID.randomUUID(), "teeeest", "teeest1", 20, 512)));
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
        System.out.println("sent packet "+packet.getClass());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof ServerInfoPacket) {
            ServerInfoPacket result = (ServerInfoPacket) packet;
            System.out.println("new ServerInfo received: "+result.getServerInfo().getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
