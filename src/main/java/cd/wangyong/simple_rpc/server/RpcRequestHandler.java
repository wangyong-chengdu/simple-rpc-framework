package cd.wangyong.simple_rpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.wangyong.simple_rpc.api.spi.Singleton;
import cd.wangyong.simple_rpc.client.ServiceTypes;
import cd.wangyong.simple_rpc.client.stubs.RpcRequest;
import cd.wangyong.simple_rpc.serialize.SerializeSupport;
import cd.wangyong.simple_rpc.transport.RequestHandler;
import cd.wangyong.simple_rpc.transport.command.Code;
import cd.wangyong.simple_rpc.transport.command.Command;
import cd.wangyong.simple_rpc.transport.command.Header;
import cd.wangyong.simple_rpc.transport.command.ResponseHeader;

/**
 * RPC请求处理
 * @author andy
 * @since 2020/10/13
 */
@Singleton
public class RpcRequestHandler implements RequestHandler, ServiceProviderRegistry {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private Map<String, Object> serviceProviders = new HashMap<>();
    
    @Override
    public Command handle(Command requestCommand) {
        Header header = requestCommand.getHeader();
        // 反序列化RpcRequest
        RpcRequest rpcRequest = SerializeSupport.parse(requestCommand.getPayload());
        try {
            Object serviceProvider = serviceProviders.get(rpcRequest.getInterfaceName());
            if (serviceProvider != null) {
                String arg = SerializeSupport.parse(rpcRequest.getSerializedArguments());
                Method method = serviceProvider.getClass().getMethod(rpcRequest.getMethodName(), String.class);
                String result = (String) method.invoke(serviceProvider, arg);
                return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId()), SerializeSupport.serialize(result));
            }
            // 如果没找到，返回NO_PROVIDER错误响应。
            logger.warn("No service Provider of {}#{}(String)!", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId(), Code.NO_PROVIDER.getCode(), "No provider!"), new byte[0]);
        } catch (Throwable t) {
            // 发生异常，返回UNKNOWN_ERROR错误响应。
            logger.warn("Exception: ", t);
            return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId(), Code.UNKNOWN_ERROR.getCode(), t.getMessage()), new byte[0]);
        }
    }

    @Override
    public int type() {
        return ServiceTypes.TYPE_RPC_REQUEST;
    }

    @Override
    public <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider) {
        serviceProviders.put(serviceClass.getCanonicalName(), serviceProvider);
        logger.info("Add service: {}, provider: {}.",
                serviceClass.getCanonicalName(),
                serviceProvider.getClass().getCanonicalName());
    }
}
