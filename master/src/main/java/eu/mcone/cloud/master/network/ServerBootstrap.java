/*
 * Copyright (c) 2017 Rufus Maiwald, Dominik L. and the MC ONE Minecraftnetwork. All rights reserved.
 *  You are not allowed to decompile the code.
 */

package eu.mcone.cloud.master.network;

import eu.mcone.cloud.core.network.pipeline.Decoder;
import eu.mcone.cloud.core.network.pipeline.Encoder;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;

public class ServerBootstrap {

    @Getter
    private int port;

    public ServerBootstrap(int port) {
        this.port = port;

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
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

            System.out.println("["+getClass().getName()+"] Netty Server starting on port "+port);
            ChannelFuture f = b.bind(port).sync().addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    System.out.println("Netty is listening @ Port:" + port);
                } else {
                    System.out.println("Failed to bind @ Port:" + port);
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