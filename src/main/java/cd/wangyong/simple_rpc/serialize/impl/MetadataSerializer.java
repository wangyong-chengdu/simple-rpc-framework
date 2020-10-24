package cd.wangyong.simple_rpc.serialize.impl;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cd.wangyong.simple_rpc.nameservice.Metadata;
import cd.wangyong.simple_rpc.serialize.Serializer;


/**
 * 用于名字服务员数据序列化
 * @see Metadata
 *
 * size of HashMap: 2字节
 * Map.entry:
 *          key: String
 *              key.length:2字节
 *              key bytes: 变长，长度由key.length决定
 *          value: list
 *              list.size: 2字节
 *              item (URI)
 *                  length: 2字节
 *                  uri bytes : 变长，长度由item.length决定
 *
 * @author andy
 * @since 2020/10/12
 */
public class MetadataSerializer implements Serializer<Metadata> {
    @Override
    public int size(Metadata entry) {
        return Short.BYTES +
                entry.entrySet().stream().mapToInt(this::entrySize).sum();
    }

    private int entrySize(Map.Entry<String, List<URI>> entry) {
        return Short.BYTES +
                entry.getKey().getBytes(StandardCharsets.UTF_8).length +
                    Short.BYTES +
                        entry.getValue().stream().mapToInt(uri -> Short.BYTES + uri.toASCIIString().getBytes(StandardCharsets.UTF_8).length).sum();
    }

    @Override
    public void serialize(Metadata entry, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);

        buffer.putShort(toShortSafely(entry.size()));

        entry.forEach((name, uris) -> {
            byte[] keyBytes = name.getBytes(StandardCharsets.UTF_8);
            buffer.putShort(toShortSafely(keyBytes.length));
            buffer.put(keyBytes);

            buffer.putShort(toShortSafely(uris.size()));
            uris.forEach(uri -> {
                byte[] uriBytes = uri.toASCIIString().getBytes(StandardCharsets.UTF_8);
                buffer.putShort(toShortSafely(uriBytes.length));
                buffer.put(uriBytes);
            });
        });
    }

    private short toShortSafely(int v) {
        assert v < Short.MAX_VALUE;
        return (short) v;
    }

    @Override
    public Metadata parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);

        Metadata metadata = new Metadata();
        short sizeOfMap = buffer.getShort();
        for (int i = 0; i < sizeOfMap; i++) {
            short keyLength = buffer.getShort();
            byte[] keyBytes = new byte[keyLength];
            buffer.get(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            int uriListSize = buffer.getShort();
            List<URI> uriList = new ArrayList<>(uriListSize);
            for (int j = 0; j < uriListSize; j++) {
                int uriLength = buffer.getShort();
                byte[] uriBytes = new byte[uriLength];
                buffer.get(uriBytes);

                URI uri = URI.create(new String(uriBytes, StandardCharsets.UTF_8));
                uriList.add(uri);
            }
            metadata.put(key, uriList);
        }
        return metadata;
    }

    @Override
    public byte type() {
        return Types.TYPE_METADATA;
    }

    @Override
    public Class<Metadata> getSerializeClass() {
        return Metadata.class;
    }
}
