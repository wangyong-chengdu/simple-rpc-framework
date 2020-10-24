package cd.wangyong.simple_rpc.transport;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.wangyong.simple_rpc.api.spi.ServiceSupport;

/**
 * 请求处理注册
 * @author andy
 * @since 2020/10/13
 */
public class RequestHandlerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerRegistry.class);
    private static volatile RequestHandlerRegistry instance;
    private Map<Integer, RequestHandler> handlerMap;

    private RequestHandlerRegistry() {
        Collection<RequestHandler> requestHandlers = ServiceSupport.loadAll(RequestHandler.class);
        handlerMap = requestHandlers.stream().collect(Collectors.toMap(RequestHandler::type, Function.identity()));
    }

    public static RequestHandlerRegistry getInstance() {
        if (instance == null) {
            synchronized (RequestHandlerRegistry.class) {
                if (instance == null) {
                    instance = new RequestHandlerRegistry();
                }
            }
        }
        return instance;
    }

    public RequestHandler get(int type) {
        return handlerMap.get(type);
    }
}
