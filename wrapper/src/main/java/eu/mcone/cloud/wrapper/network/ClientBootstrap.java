package eu.mcone.cloud.wrapper.network;

import eu.mcone.cloud.core.network.pipeline.Decoder;
import eu.mcone.cloud.core.network.pipeline.Encoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

public class ClientBootstrap {

    @Getter
    private int port;
    @Getter
    private ChannelPacketHandler cph;
    private Channel ch;

    public ClientBootstrap(String host, int port) {
        this.port = port;

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            cph = new ChannelPacketHandler();

                            ch.pipeline().addLast(new Decoder());
                            ch.pipeline().addLast(new Encoder());
                            ch.pipeline().addLast(cph);
                        }
                    });

            ChannelFuture f =b.connect(host, port).sync().addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    System.out.println("Netty is connected @ Port:" + port);
                } else {
                    System.out.println("Failed to connect to @ Port:" + port);
                }
            }).addListener(ChannelFutureListener.CLOSE_ON_FAILURE).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
