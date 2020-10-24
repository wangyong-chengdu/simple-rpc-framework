package cd.wangyong.simple_rpc.transport;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * @author andy
 * @since 2020/10/13
 */
public interface TransportClient extends Closeable {
    Transport createTransport(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException;

    @Override
    void close() throws IOException;
}
