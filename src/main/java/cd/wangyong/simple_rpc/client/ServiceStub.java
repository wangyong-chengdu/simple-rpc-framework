package cd.wangyong.simple_rpc.client;

import cd.wangyong.simple_rpc.transport.Transport;

/**
 * @author andy
 * @since 2020/10/12
 */
public interface ServiceStub {
    void setTransport(Transport transport);
}
