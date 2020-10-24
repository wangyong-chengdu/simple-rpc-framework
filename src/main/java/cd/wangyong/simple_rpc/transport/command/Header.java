package cd.wangyong.simple_rpc.transport.command;

/**
 * @author andy
 * @since 2020/10/12
 */
public class Header {
    /**
     * 用于请求和响应的配对
     */
    private int requestId;
    /**
     * 版本号，用于协议向下兼容
     */
    private int version;
    /**
     * 命令类型，用于路由到对应的处理类
     */
    private int type;

    public Header() {
    }

    public Header(int type, int version, int requestId) {
        this.type = type;
        this.version = version;
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int length() {
        return Integer.BYTES + Integer.BYTES + Integer.BYTES;
    }
}
