package cd.wangyong.simple_rpc.transport.netty;

import java.util.concurrent.CompletableFuture;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import cd.wangyong.simple_rpc.transport.InFlightRequests;
import cd.wangyong.simple_rpc.transport.ResponseFuture;
import cd.wangyong.simple_rpc.transport.Transport;
import cd.wangyong.simple_rpc.transport.command.Command;

/**
 * @author andy
 * @since 2020/10/12
 */
public class NettyTransport implements Transport {
    private final Channel channel;
    private final InFlightRequests inFlightRequests;

    public NettyTransport(Channel channel, InFlightRequests inFlightRequests) {
        this.channel = channel;
        this.inFlightRequests = inFlightRequests;
    }

    @Override
    public CompletableFuture<Command> send(Command request) {
        CompletableFuture<Command> completableFuture = new CompletableFuture<>();
        try {
            inFlightRequests.put(new ResponseFuture(request.getHeader().getRequestId(), completableFuture));
            // 发送命令
            channel.writeAndFlush(request).addListener((ChannelFutureListener)channelFuture -> {
               // 处理发送失败的情况
                if (!channelFuture.isSuccess()) {
                    completableFuture.completeExceptionally(channelFuture.cause());
                    channel.close();
                }
            });
        } catch (Throwable t) {
            inFlightRequests.remove(request.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }
}
