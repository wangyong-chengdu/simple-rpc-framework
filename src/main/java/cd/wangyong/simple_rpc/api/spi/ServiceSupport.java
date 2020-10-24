package cd.wangyong.simple_rpc.api.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author andy
 * @since 2020/10/12
 */
public class ServiceSupport {
    private static final Map<String, Object> singletonServices = new HashMap<>();

    /**
     * 加载接口实现类引用
     * @param service 接口Class
     */
    public synchronized static <S> S load(Class<S> service) {
        return StreamSupport
                .stream(ServiceLoader.load(service).spliterator(), false)
                .map(ServiceSupport::singletonFilter)
                .findFirst().orElseThrow(ServiceLoadException::new);
    }

    @SuppressWarnings("unchecked")
    private static <S> S singletonFilter(S service) {
        if (service.getClass().isAnnotationPresent(Singleton.class)) {
            String className = service.getClass().getCanonicalName();
            Object singletonInstance = singletonServices.putIfAbsent(className, service);
            return singletonInstance != null ? (S) singletonInstance : service;
        }
        else {
            return service;
        }
    }

    /**
     * 加载接口所有实现类实例
     */
    public synchronized static <S> Collection<S> loadAll(Class<S> service) {
        return StreamSupport
                .stream(ServiceLoader.load(service).spliterator(), false)
                .map(ServiceSupport::singletonFilter)
                .collect(Collectors.toList());
    }
}
