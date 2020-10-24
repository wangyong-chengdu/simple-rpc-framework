package cd.wangyong.simple_rpc.server;

/**
 * 注册服务提供者
 * @author andy
 * @since 2020/10/13
 */
public interface ServiceProviderRegistry {
    /**
     * 增加服务提供者
     * @param serviceClass 接口Class
     * @param serviceProvider 接口实现实例
     * @param <T> 服务实现类型
     */
    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);
}
