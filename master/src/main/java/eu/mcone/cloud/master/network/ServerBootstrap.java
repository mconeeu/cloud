/*
 * Copyright (c) 2017 - 2018 Rufus Maiwald, Dominik Lippl and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.console.Logger;
import eu.mcone.cloud.core.network.pipeline.Decoder;
import eu.mcone.cloud.core.network.pipeline.Encoder;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;

public class ServerBootstrap {

    private static final boolean EPOLL = Epoll.isAvailable();
    @Getter
    private int port;

    public ServerBootstrap(int port) {
        this.port = port;

        EventLoopGroup bossGroup = EPOLL ? new EpollEventLoopGroup(4) : new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = EPOLL ? new EpollEventLoopGroup(4) : new NioEventLoopGroup(4);

        try {
            io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Decoder());
                            ch.pipeline().addLast(new Encoder());
                            ch.pipeline().addLast(new ChannelPacketHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync().addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    Logger.log(getClass(), "Netty is listening @ Port:" + port);
                } else {
                    Logger.log(getClass(), "Failed to bind @ Port:" + port);
                }
            }).addListener(ChannelFutureListener.CLOSE_ON_FAILURE).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }



}
