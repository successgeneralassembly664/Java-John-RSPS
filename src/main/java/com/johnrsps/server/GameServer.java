package com.johnrsps.server;

import com.johnrsps.game.Npc;
import com.johnrsps.game.Player;
import com.johnrsps.game.World;
import com.johnrsps.network.GameServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameServer {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private static final int PORT = 43594;
    private static final ScheduledExecutorService gameTickExecutor = Executors.newSingleThreadScheduledExecutor();

    public void start() throws InterruptedException {
        // Spawn a single NPC on server start (ID: 1, coordinates: 3222, 3222)
        World.addNpc(new Npc(1, 3222, 3222));
        gameTickExecutor.scheduleAtFixedRate(World::gameTick, 0, 600, TimeUnit.MILLISECONDS);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new com.johnrsps.packet.PacketDecoder());
                        ch.pipeline().addLast(new GameServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(PORT).sync();
            logger.info("Server started on port {}", PORT);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            gameTickExecutor.shutdownNow();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new GameServer().start();
    }
}