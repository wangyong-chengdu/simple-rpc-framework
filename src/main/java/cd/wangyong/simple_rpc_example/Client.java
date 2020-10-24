package cd.wangyong.simple_rpc_example;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.wangyong.simple_rpc.api.NameService;
import cd.wangyong.simple_rpc.api.RpcAccessPoint;
import cd.wangyong.simple_rpc.api.spi.ServiceSupport;
import cd.wangyong.simple_rpc_example.api.HelloService;

/**
 * @author andy
 * @since 2020/10/11
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {
        File nameServiceData = new File(new File(System.getProperty("java.io.tmpdir")), "cd.wangyong.simple_rpc2_name_service.data");
        String serviceName = HelloService.class.getCanonicalName();

        try (RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class)) {
            // 查找服务，获取服务实例
            NameService nameService = rpcAccessPoint.getNameService(nameServiceData.toURI());
            URI uri = nameService.lookupService(serviceName);
            assert uri != null;
            logger.info("找到服务{}，提供者: {}.", serviceName, uri);
            HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class);

            // 接口调用
            String name = "wangyong-chengdu";
            logger.info("请求服务, name: {}...", name);
            String response = helloService.hello(name);
            logger.info("收到响应: {}.", response);
        }
    }
}
