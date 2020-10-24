package cd.wangyong.simple_rpc_example.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.wangyong.simple_rpc_example.api.HelloService;

/**
 * @author andy
 * @since 2020/10/11
 */
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("HelloServiceImpl收到: {}.", name);
        String ret = "Hello, " + name;
        logger.info("HelloServiceImpl返回: {}.", ret);
        return ret;
    }
}
