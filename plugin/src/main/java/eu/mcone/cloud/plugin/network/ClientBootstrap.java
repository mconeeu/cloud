/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.plugin.network;

import eu.mcone.cloud.core.network.pipeline.Decoder;
import eu.mcone.cloud.core.network.pipeline.Encoder;
import eu.mcone.cloud.plugin.CloudPlugin;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

public class ClientBootstrap {

    private static final boolean EPOLL = Epoll.isAvailable();

    private String host;
    private int port, reconnectTrys;

    @Getter
    private CloudPlugin instance;

    public ClientBootstrap(String host, int port, CloudPlugin instance) {
        this.host = host;
        this.port = port;
        this.instance = instance;

        tryConnect();
    }

    private void tryConnect() {
        instance.getPlugin().runAsync(() -> {
            EventLoopGroup workerGroup = EPOLL ? new EpollEventLoopGroup(4) : new NioEventLoopGroup(4);

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(EPOLL ? EpollSocketChannel.class : NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new Decoder());
                                ch.pipeline().addLast(new Encoder());
                                ch.pipeline().addLast(new ChannelPacketHandler(instance));
                            }
                        });

                ChannelFuture f = bootstrap.connect(host, port).sync();
                f.addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        System.out.println("Netty is connected @ Port:" + port);
                        reconnectTrys = 0;
                    } else {
                        System.out.println("Failed to connect to @ Port:" + port);
                    }
                });

                f.channel().closeFuture().sync();
            } catch (Exception e) {
                reconnectTrys++;

                workerGroup.shutdownGracefully();

                System.err.println("Could not connect to Master. Reconnecting... ["+reconnectTrys+"]");
                System.err.println(e.getMessage());
            } finally {
                System.out.println("shutdown workergroup");
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
