package io.dongtai.iast.agent.middlewarerecognition.servlet;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletService implements IServer {

    public static final String NAME = "Servlet";

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {

        try {
            loader.loadClass(" javax.servlet.ServletRequest".substring(1));
            return true;
        } catch (Throwable ignore) {
        }

        try {
            // tomcat 10之后的Servlet改为了这个
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
        // TODO 2023-9-28 14:31:56 为什么其它的实现没有版本的时候返回的是null这里返回的却是空字符串？是不是有什么特殊原因
        return "";
    }
}
