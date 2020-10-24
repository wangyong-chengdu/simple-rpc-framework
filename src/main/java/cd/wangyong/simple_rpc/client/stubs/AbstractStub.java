package cd.wangyong.simple_rpc.client.stubs;

import cd.wangyong.simple_rpc.client.RequestIdSupport;
import cd.wangyong.simple_rpc.client.ServiceStub;
import cd.wangyong.simple_rpc.client.ServiceTypes;
import cd.wangyong.simple_rpc.serialize.SerializeSupport;
import cd.wangyong.simple_rpc.transport.Transport;
import cd.wangyong.simple_rpc.transport.command.Code;
import cd.wangyong.simple_rpc.transport.command.Command;
import cd.wangyong.simple_rpc.transport.command.Header;
import cd.wangyong.simple_rpc.transport.command.ResponseHeader;

/**
 * @author andy
 * @since 2020/10/12
 */
public abstract class AbstractStub implements ServiceStub {
    protected Transport transport;

    protected byte[] invokeRemote(RpcRequest request) {
        try {
            Header header = new Header(ServiceTypes.TYPE_RPC_REQUEST, 1, RequestIdSupport.next());
            byte[] payload = SerializeSupport.serialize(request);
            Command requestCommand = new Command(header, payload);

            Command responseCommand = transport.send(requestCommand).get();
            ResponseHeader responseHeader = (ResponseHeader) responseCommand.getHeader();
            if (responseHeader.getCode() == Code.SUCCESS.getCode()) {
                return responseCommand.getPayload();
            }
            else {
                throw new RuntimeException(responseHeader.getError());
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }
}
