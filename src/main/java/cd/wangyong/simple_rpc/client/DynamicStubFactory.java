package cd.wangyong.simple_rpc.client;

import java.util.Map;

import com.itranswarp.compiler.JavaStringCompiler;

import cd.wangyong.simple_rpc.transport.Transport;

/**
 * @author andy
 * @since 2020/10/12
 */
public class DynamicStubFactory implements StubFactory{
    private final static String STUB_SOURCE_TEMPLATE =
            "package cd.wangyong.simple_rpc2.client.stubs;\n" +
                    "import cd.wangyong.simple_rpc2.serialize.SerializeSupport;\n" +
                    "\n" +
                    "public class %s extends AbstractStub implements %s {\n" +
                    "    @Override\n" +
                    "    public String %s(String arg) {\n" +
                    "        return SerializeSupport.parse(\n" +
                    "                invokeRemote(\n" +
                    "                        new RpcRequest(\n" +
                    "                                \"%s\",\n" +
                    "                                \"%s\",\n" +
                    "                                SerializeSupport.serialize(arg)\n" +
                    "                        )\n" +
                    "                )\n" +
                    "        );\n" +
                    "    }\n" +
                    "}";


    @Override
    @SuppressWarnings("unchecked")
    public <T> T createStub(Transport transport, Class<T> serviceClass) {
        try {
            // 构造源代码
            String stubSimpleName = serviceClass.getSimpleName() + "Stub";
            String classFullName = serviceClass.getName();
            String methodName = serviceClass.getMethods()[0].getName();
            String sourceCode = String.format(STUB_SOURCE_TEMPLATE, stubSimpleName, classFullName, methodName, classFullName, methodName);

            // 编译源代码
            JavaStringCompiler compiler = new JavaStringCompiler();
            Map<String, byte[]> result = compiler.compile(stubSimpleName + ".java", sourceCode);

            // 加载编译好的类
            String stubFullName = "cd.wangyong.simple_rpc2.client.stubs." + stubSimpleName;
            Class<?> clazz = compiler.loadClass(stubFullName, result);

            // 实例化
            ServiceStub stub = (ServiceStub)clazz.newInstance();
            stub.setTransport(transport);
            return (T) stub;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
