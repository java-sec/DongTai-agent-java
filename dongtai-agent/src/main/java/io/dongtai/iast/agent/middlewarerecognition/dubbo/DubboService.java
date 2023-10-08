package io.dongtai.iast.agent.middlewarerecognition.dubbo;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * 检查是否是dubbo服务
 *
 * @author CC11001100
 */
public class DubboService implements IServer {

    public static final String NAME = "Dubbo";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {

        try {
            // 后面这个apache的版本会使用得越来越多，所以此项放在前面优先命中
            // 2.7.0开始，直接使用org.apache.dubbo
            loader.loadClass(" org.apache.dubbo.monitor.support.MonitorFilter".substring(1));
            return true;
        } catch (Throwable ignored) {
        }

        try {
            // 如果2.6.x及以下版本，可以使用：com.alibaba.dubbo，
            loader.loadClass(" com.alibaba.dubbo.monitor.support.MonitorFilter".substring(1));
            return true;
        } catch (Throwable ignored) {
        }

        return false;
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
