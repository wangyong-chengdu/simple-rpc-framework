package cd.wangyong.simple_rpc.nameservice;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.wangyong.simple_rpc.api.NameService;
import cd.wangyong.simple_rpc.serialize.SerializeSupport;

/**
 * @author andy
 * @since 2020/10/12
 */
public class LocalFileNameService implements NameService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileNameService.class);

    private static final Collection<String> SCHEMES = Collections.singleton("file");
    private File file;


    @Override
    public synchronized void registerService(String serviceName, URI uri) throws IOException {
        logger.info("Register service:{}, uri:{}.", serviceName, uri);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel fileChannel = raf.getChannel()) {
            // 操作系统提供的文件锁
            FileLock lock = fileChannel.lock();
            try {
                // 读取元数据并进行更新
                Metadata metadata = readOrBuildMetadata(raf);
                List<URI> uris = metadata.computeIfAbsent(serviceName, k -> new ArrayList<>());
                if (!uris.contains(uri)) {
                    uris.add(uri);
                }
                logger.info(metadata.toString());

                // 覆盖文件
                byte[] newBytes = SerializeSupport.serialize(metadata);
                fileChannel.truncate(newBytes.length);
                fileChannel.position(0L);
                fileChannel.write(ByteBuffer.wrap(newBytes));
                fileChannel.force(true);
            } finally {
                lock.release();
            }
        }

    }

    private Metadata readOrBuildMetadata(RandomAccessFile raf) throws IOException {
        long fileLength = raf.length();
        if (fileLength > 0) {
            byte[] bytes = new byte[(int) fileLength];
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) {
                raf.getChannel().read(buffer);
            }
            return SerializeSupport.parse(bytes);
        }
        else {
            return new Metadata();
        }
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        Metadata metadata;
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = raf.getChannel()) {
            // 操作系统提供的文件锁
            FileLock lock = fileChannel.lock();
            try {
                // 读取元数据
                metadata = readOrBuildMetadata(raf);

                // 查找URI
                List<URI> uris = metadata.get(serviceName);
                if(null != uris && !uris.isEmpty()) {
                    return uris.get(ThreadLocalRandom.current().nextInt(uris.size()));
                }
            } finally {
                lock.release();
            }
        }
        return null;
    }

    @Override
    public Collection<String> supportedSchemes() {
        return SCHEMES;
    }

    @Override
    public void connect(URI nameServiceUri) {
        if (SCHEMES.contains(nameServiceUri.getScheme())) {
            file = new File(nameServiceUri);
        }
        else {
            throw new RuntimeException("Unsupported scheme!");
        }
    }
}
