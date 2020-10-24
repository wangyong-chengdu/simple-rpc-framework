package cd.wangyong.simple_rpc.api;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * 名字服务（即注册中心）
 * @author andy
 * @since 2020/10/11
 */
public interface NameService {

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param uri 服务地址
     */
    void registerService(String serviceName, URI uri) throws IOException;

    /**
     * 查找服务（即服务发现）
     * @param serviceName 服务名称
     * @return 服务地址
     */
    URI lookupService(String serviceName) throws IOException;

    /**
     * 所有支持的协议
     */
    Collection<String> supportedSchemes();

    /**
     * 给定注册中心服务端URI，去建立与注册中心的连接
     * @param nameServiceUri 注册中心地址
     */
    void connect(URI nameServiceUri);

}
