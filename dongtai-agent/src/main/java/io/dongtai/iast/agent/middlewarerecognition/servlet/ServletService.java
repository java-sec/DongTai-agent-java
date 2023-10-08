package io.dongtai.iast.agent.middlewarerecognition.servlet;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 * @author CC11001100
 */
public class ServletService implements IServer {

    public static final String NAME = "Servlet";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {

        try {
            loader.loadClass(" javax.servlet.ServletRequest".substring(1));
            return true;
        } catch (Throwable ignore) {
        }

        try {
            // tomcat 10之后的Servlet改为了这个包
            loader.loadClass(" jakarta.servlet.ServletRequest".substring(1));
            return true;
        } catch (Throwable ignore) {
        }

        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        // TODO 2023-10-8 11:45:14 是否有必要区分Servlet的不同版本呢？
        return null;
    }

}
