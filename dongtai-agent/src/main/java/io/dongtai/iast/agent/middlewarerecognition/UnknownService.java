package io.dongtai.iast.agent.middlewarerecognition;

import io.dongtai.log.DongTaiLog;

import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * TODO 看起来是处理通过maven或者gradle直接启动的方式的？已经没有人知道是因为什么了...先保留着这个逻辑吧...
 */
public class UnknownService implements IServer {

    public static final String NAME = "Unknown";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        Map<String, String> properties = runtimeMXBean.getSystemProperties();
        String sunJavaCommand = properties.get("sun.java.command");
        boolean isMatched = sunJavaCommand != null &&
                !sunJavaCommand.contains("org.gradle.launcher.GradleMain") &&
                !sunJavaCommand.contains("org.codehaus.plexus.classworlds.launcher.Launcher");
        if (isMatched) {
            DongTaiLog.info("middleware recognition is unknown service");
        }
        return isMatched;
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
