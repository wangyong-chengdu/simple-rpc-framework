package cd.wangyong.simple_rpc.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import cd.wangyong.simple_rpc.transport.RequestHandlerRegistry;
import cd.wangyong.simple_rpc.transport.TransportServer;

/**
 * @author andy
 * @since 2020/10/13
 */
public class NettyServer implements TransportServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private int port;
    private EventLoopGroup acceptEventGroup;
    private EventLoopGroup ioEventGroup;
    private Channel channel;
    private RequestHandlerRegistry requestHandlerRegistry;

    @Override
    public void start(RequestHandlerRegistry requestHandlerRegistry, int port) throws Exception {
        this.port = port;
        this.requestHandlerRegistry = requestHandlerRegistry;
        EventLoopGroup acceptEventGroup = newEventLoopGroup();
        EventLoopGroup ioEventGroup = newEventLoopGroup();
        ChannelHandler channelHandlerPipeline = newChannelHandlerPipeline();
        ServerBootstrap serverBootstrap = newBootstrap(channelHandlerPipeline, acceptEventGroup, ioEventGroup);
        Channel channel = doBind(serverBootstrap);

        this.acceptEventGroup = acceptEventGroup;
        this.ioEventGroup = ioEventGroup;
        this.channel = channel;
    }

    private Channel doBind(ServerBootstrap serverBootstrap) throws InterruptedException {
        return serverBootstrap.bind(port)
                .sync()
                .channel();
    }

    private ServerBootstrap newBootstrap(ChannelHandler channelHandler, EventLoopGroup acceptEventGroup, EventLoopGroup ioEventGroup) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .group(acceptEventGroup, ioEventGroup)
                .childHandler(channelHandler)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return serverBootstrap;
    }

    private ChannelHandler newChannelHandlerPipeline() {
        return new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new RequestDecoder()) // request解码，主要出Command对象
                        .addLast(new ResponseEncoder()) // response编码，将Command对象变成字节码
                        .addLast(new RequestInvocation(requestHandlerRegistry)); // 请求处理
            }
        };
    }

    private EventLoopGroup newEventLoopGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup();
        }
        else {
            return new NioEventLoopGroup();
        }
    }

    @Override
    public void stop() {
        if (acceptEventGroup != null) {
            acceptEventGroup.shutdownGracefully();
        }
        if (ioEventGroup != null) {
            ioEventGroup.shutdownGracefully();
        }
        if (channel != null) {
            channel.close();
        }
    }
}
