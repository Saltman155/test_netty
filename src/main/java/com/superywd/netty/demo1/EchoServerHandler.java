package com.superywd.netty.demo1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * ChannelInboundHandlerAdapter实现了接口 ChannelInboundHandler，也就是接口channelHandler
 *
 * channelHandler是netty中的事物驱动接口实现类，一般在这个类中实现我们的业务方法，或者做编&解码器的操作
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    /**
     * 当对每条读取的数据都要调用
     * @param ctx       上下文对象
     * @param msg       具体的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf)msg;
        String message = in.toString(CharsetUtil.UTF_8);
        logger.info("服务端接收："+message);
        // 这里用flush和不用flush的区别是不一样的
        ctx.write(Unpooled.copiedBuffer("服务端已经收到你的消息了",CharsetUtil.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
