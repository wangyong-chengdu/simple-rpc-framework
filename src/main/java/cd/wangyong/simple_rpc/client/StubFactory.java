package cd.wangyong.simple_rpc.client;

import cd.wangyong.simple_rpc.transport.Transport;

/**
 * @author andy
 * @since 2020/10/12
 */
public interface StubFactory {
    /**
     * 创建桩（服务代理实例）
     * @param transport 网络传输
     * @param serviceClass 接口Class
     * @param <T> 接口类型
     * @return 服务代理实例
     */
    <T> T createStub(Transport transport, Class<T> serviceClass);
}
