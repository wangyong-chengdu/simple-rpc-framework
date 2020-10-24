package cd.wangyong.simple_rpc;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import cd.wangyong.simple_rpc.api.RpcAccessPoint;
import cd.wangyong.simple_rpc.api.spi.ServiceSupport;
import cd.wangyong.simple_rpc.client.StubFactory;
import cd.wangyong.simple_rpc.server.ServiceProviderRegistry;
import cd.wangyong.simple_rpc.transport.RequestHandlerRegistry;
import cd.wangyong.simple_rpc.transport.Transport;
import cd.wangyong.simple_rpc.transport.TransportClient;
import cd.wangyong.simple_rpc.transport.TransportServer;

/**
 * @author andy
 * @since 2020/10/13
 */
public class NettyRpcAccessPoint implements RpcAccessPoint {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private static final URI URI_1 = URI.create("rpc://" + HOST + ":" + PORT);

    private TransportServer server;

    private TransportClient client = ServiceSupport.load(TransportClient.class);
    private final Map<URI, Transport> clientMap = new ConcurrentHashMap<>();
    private final StubFactory stubFactory = ServiceSupport.load(StubFactory.class);

    private final ServiceProviderRegistry serviceProviderRegistry = ServiceSupport.load(ServiceProviderRegistry.class);

    @Override
    public <T> T getRemoteService(URI uri, Class<T> serviceClass) {
        Transport transport = clientMap.computeIfAbsent(uri, this::createTransport);
        return stubFactory.createStub(transport, serviceClass);
    }

    private Transport createTransport(URI uri) {
        try {
            return client.createTransport(new InetSocketAddress(uri.getHost(), uri.getPort()), 30000L);
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized <T> URI addServiceProvider(T service, Class<T> serviceClass) {
        serviceProviderRegistry.addServiceProvider(serviceClass, service);
        return URI_1;
    }

    @Override
    public synchronized Closeable startService() throws Exception {
        if (server == null) {
            server = ServiceSupport.load(TransportServer.class);
            server.start(RequestHandlerRegistry.getInstance(), PORT);
        }
        return () -> {
            if (server != null) {
                server.stop();
            }
        };
    }

    @Override
    public void close() throws IOException {
        if (server != null) {
            server.stop();;
        }
        client.close();
    }
}
