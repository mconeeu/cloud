/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.wrapper.network;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.pipeline.Decoder;
import eu.mcone.cloud.core.network.pipeline.Encoder;
import eu.mcone.cloud.wrapper.WrapperServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientBootstrap {

    private static final boolean EPOLL = Epoll.isAvailable();

    private String host;
    private int port, reconnectTrys;

    @Getter
    private Channel channel;

    public ClientBootstrap(String host, int port) {
        this.host = host;
        this.port = port;

        tryConnect();
    }

    private void tryConnect() {
        WrapperServer.getInstance().getThreadPool().execute(() -> {
            EventLoopGroup workerGroup = EPOLL ? new EpollEventLoopGroup(4) : new NioEventLoopGroup(4);

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(EPOLL ? EpollServerSocketChannel.class : NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new Decoder());
                                ch.pipeline().addLast(new Encoder());
                                ch.pipeline().addLast(new ChannelPacketHandler());
                            }
                        });

                ChannelFuture f = bootstrap.connect(host, port).sync();
                f.addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        Logger.log(getClass(), "Netty is connected @ Port:" + port);
                        reconnectTrys = 0;
                    } else {
                        Logger.log(getClass(), "Failed to connect to @ Port:" + port);
                    }
                });

                channel = f.channel();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                reconnectTrys++;

                workerGroup.shutdownGracefully();

                Logger.err(getClass(), "Could not connect to Master. Reconnecting... ["+reconnectTrys+"]");
                Logger.err(getClass(), e.getMessage());
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
    }

    void scheduleReconnect() {
        try {
            Thread.sleep(5000);
            tryConnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
