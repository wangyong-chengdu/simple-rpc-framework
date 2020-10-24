package cd.wangyong.simple_rpc.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import cd.wangyong.simple_rpc.transport.command.Command;
import cd.wangyong.simple_rpc.transport.command.Header;

/**
 * @author andy
 * @since 2020/10/13
 */
public abstract class CommandEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        if (!(msg instanceof Command)) {
            throw new Exception(String.format("Unknown type: %s", msg.getClass().getCanonicalName()));
        }

        Command command = (Command) msg;
        byteBuf.writeInt(Integer.BYTES + command.getHeader().length() + command.getPayload().length);
        encodeHeader(channelHandlerContext, command.getHeader(), byteBuf);
        byteBuf.writeBytes(command.getPayload());
    }

    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, Header header, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(header.getType());
        byteBuf.writeInt(header.getVersion());
        byteBuf.writeInt(header.getRequestId());
    }
}
