package cd.wangyong.simple_rpc.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import cd.wangyong.simple_rpc.transport.command.Header;

/**
 * @author andy
 * @since 2020/10/13
 */
public class RequestDecoder extends CommandDecoder{
    @Override
    protected Header decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        return new Header(byteBuf.readInt(), byteBuf.readInt(), byteBuf.readInt());
    }
}
