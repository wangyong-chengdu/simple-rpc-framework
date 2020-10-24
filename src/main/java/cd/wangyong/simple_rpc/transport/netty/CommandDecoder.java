package cd.wangyong.simple_rpc.transport.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import cd.wangyong.simple_rpc.transport.command.Command;
import cd.wangyong.simple_rpc.transport.command.Header;

/**
 * @author andy
 * @since 2020/10/13
 */
public abstract class CommandDecoder extends ByteToMessageDecoder {
    private static final int FIELD_LENGTH = Integer.BYTES;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (!byteBuf.isReadable(FIELD_LENGTH)) {
            return;
        }

        byteBuf.markReaderIndex();
        int length = byteBuf.readInt() - FIELD_LENGTH;
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        Header header = decodeHeader(channelHandlerContext, byteBuf);
        int payloadLength  = length - header.length();
        byte [] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        list.add(new Command(header, payload));
    }

    protected abstract Header decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf);
}
