package com.johnrsps.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles incoming client connections and 317 protocol packets
 */
public class GameServerHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(GameServerHandler.class);
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientIP = ctx.channel().remoteAddress().toString();
        logger.info("Client connected: {}", clientIP);
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientIP = ctx.channel().remoteAddress().toString();
        logger.info("Client disconnected: {}", clientIP);
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        
        try {
            // 317 Protocol packet handling
            byte opcode = buf.readByte();
            int length = buf.readableBytes();
            
            logger.debug("Received packet - Opcode: {} Length: {}", opcode, length);
            
            // Handle based on connection state
            handlePacket(ctx, opcode, buf);
        } finally {
            buf.release();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Channel error: {}", cause.getMessage());
        ctx.close();
    }
    
    /**
     * Handle incoming 317 protocol packets
     */
    private void handlePacket(ChannelHandlerContext ctx, byte opcode, ByteBuf buf) {
        switch (opcode) {
            case 0: // Login request
                logger.info("Login request received");
                // TODO: Implement login handler
                break;
            case 1: // Game packet
                logger.debug("Game packet received");
                // TODO: Implement game packet handler
                break;
            default:
                logger.warn("Unknown opcode: {}", opcode);
                break;
        }
    }
}