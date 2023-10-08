package io.dongtai.iast.agent.middlewarerecognition.spring;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 * @author CC11001100
 */
public class SpringService implements IServer {

    public static final String NAME = "Spring";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
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
        return null;
    }

}
