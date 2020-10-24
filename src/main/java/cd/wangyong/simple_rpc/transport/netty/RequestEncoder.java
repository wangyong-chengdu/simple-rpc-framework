package cd.wangyong.simple_rpc.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import cd.wangyong.simple_rpc.transport.command.Header;

/**
 * @author andy
 * @since 2020/10/13
 */
public class RequestEncoder extends CommandEncoder {
    @Override
    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, Header header, ByteBuf byteBuf) throws Exception {
        super.encodeHeader(channelHandlerContext, header, byteBuf);
    }
}
