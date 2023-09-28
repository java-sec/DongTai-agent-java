package io.dongtai.iast.agent.middlewarerecognition.spring;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SpringService implements IServer {

    public static final String NAME = "Spring";

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        try {
            loader.loadClass(" org.springframework.web.context.ConfigurableWebApplicationContext".substring(1));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        // TODO 2023-9-28 14:31:56 为什么其它的实现没有版本的时候返回的是null这里返回的却是空字符串？是不是有什么特殊原因
        return "";
    }

}
