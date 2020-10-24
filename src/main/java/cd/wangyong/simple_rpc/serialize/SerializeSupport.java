package cd.wangyong.simple_rpc.serialize;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.wangyong.simple_rpc.api.spi.ServiceSupport;

/**
 * 序列化门面
 * @author andy
 * @since 2020/10/12
 */
public class SerializeSupport {
    private static final Logger logger = LoggerFactory.getLogger(SerializeSupport.class);

    /**
     * 用于序列化时根据待序列化对象类型查找对应的序列化实现
     */
    private static Map<Class<?>/*序列化对象类型*/, Serializer<?>/*序列化实现*/> serializerMap = new HashMap<>();
    /**
     * 用于反序列化时，从序列化数据中读出对象类型，然后找到对应的待序对象Class
     */
    private static Map<Byte/*待序列化对象类型*/, Class<?>/*待序列化对象Class类型*/> typeMap = new HashMap<>();

    static {
        ServiceSupport.loadAll(Serializer.class).forEach(serializer -> {
            serializerMap.put(serializer.getSerializeClass(), serializer);
            typeMap.put(serializer.type(), serializer.getSerializeClass());
            logger.info("Found serializer, class: {}, type: {}", serializer.getSerializeClass().getCanonicalName(), serializer.type());
        });
    }

    @SuppressWarnings("unchecked")
    public static <E> E parse(byte[] buffer) {
        byte type = buffer[0];
        Class<E> aClass = (Class<E>) typeMap.get(type);
        if (aClass == null) {
            throw new SerializeException(String.format("Unknown entry type: %d!", type));
        }

        Object entry = serializerMap.get(aClass).parse(buffer, 1, buffer.length - 1);
        if (aClass.isAssignableFrom(entry.getClass())) {
            return (E) entry;
        }
        throw new SerializeException("Type mismatch!");
    }
    
    public static <E> byte[] serialize(E entry) {
        @SuppressWarnings("unchecked")
        Serializer<E> serializer = (Serializer<E>) serializerMap.get(entry.getClass());
        if (serializer == null) {
            throw new SerializeException(String.format("Unknown entry class type: %s", entry.getClass().toString()));
        }

        byte[] bytes = new byte[serializer.size(entry) + 1];
        bytes[0] = serializer.type();
        serializer.serialize(entry, bytes, 1, bytes.length - 1);
        return bytes;
    }

}
