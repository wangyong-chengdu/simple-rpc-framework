package cd.wangyong.simple_rpc.nameservice;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * 名字服务源数据
 * @author andy
 * @since 2020/10/12
 */
public class Metadata extends HashMap<String/*服务名*/, List<URI>/*服务名提供者URI*/> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Metadata:").append("\n");
        this.forEach((key, uris) -> {
            sb.append("\t").append("Classname:").append(key).append("\n");
            sb.append("\t").append("URIs:").append("\n");
            uris.forEach(uri -> sb.append("\t\t").append(uri).append("\n"));
        });
        return sb.toString();
    }
}
