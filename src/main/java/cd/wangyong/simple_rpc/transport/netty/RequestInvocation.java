package cd.wangyong.simple_rpc.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import cd.wangyong.simple_rpc.transport.RequestHandler;
import cd.wangyong.simple_rpc.transport.RequestHandlerRegistry;
import cd.wangyong.simple_rpc.transport.command.Command;

/**
 * @author andy
 * @since 2020/10/13
 */
@ChannelHandler.Sharable
public class RequestInvocation extends SimpleChannelInboundHandler<Command> {
    private static final Logger logger = LoggerFactory.getLogger(RequestInvocation.class);

    /**
     * 请求处理注册中心，入口，用于分发请求处理
     */
    private final RequestHandlerRegistry requestHandlerRegistry;

    public RequestInvocation(RequestHandlerRegistry requestHandlerRegistry) {
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command request) throws Exception {
        RequestHandler handler = requestHandlerRegistry.get(request.getHeader().getType());
        if (handler != null) {
            Command response = handler.handle(request);
            if (response != null) {
                channelHandlerContext.writeAndFlush(response).addListener((ChannelFutureListener)  channelFuture -> {
                    if (!channelFuture.isSuccess()) {
                        logger.warn("Write response failed!", channelFuture.cause());
                        channelHandlerContext.channel().close();
                    }
                });
            }
            else {
                logger.warn("Response is null!");
            }
        }
        else {
            throw new Exception(String.format("No handler for request with type: %d!", request.getHeader().getType()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Exception: ", cause);

        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) ctx.close();
    }
}
