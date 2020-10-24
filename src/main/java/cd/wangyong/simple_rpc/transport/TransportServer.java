package cd.wangyong.simple_rpc.transport;

/**
 * @author andy
 * @since 2020/10/13
 */
public interface TransportServer {
    /**
     * 启动
     * @param requestHandlerRegistry 请求处理Registry
     */
    void start(RequestHandlerRegistry requestHandlerRegistry, int port) throws Exception;

    void stop();
}
