package io.dongtai.iast.agent.middlewarerecognition;

import io.dongtai.iast.agent.AgentLauncher;
import io.dongtai.iast.agent.middlewarerecognition.dubbo.DubboService;
import io.dongtai.iast.agent.middlewarerecognition.gRPC.GrpcService;
import io.dongtai.iast.agent.middlewarerecognition.jboss.JBoss;
import io.dongtai.iast.agent.middlewarerecognition.jboss.JBossAS;
import io.dongtai.iast.agent.middlewarerecognition.jetty.Jetty;
import io.dongtai.iast.agent.middlewarerecognition.servlet.ServletService;
import io.dongtai.iast.agent.middlewarerecognition.spring.SpringService;
import io.dongtai.iast.agent.middlewarerecognition.spring.Tomcat;
import io.dongtai.iast.agent.middlewarerecognition.tomcat.*;
import io.dongtai.iast.agent.middlewarerecognition.weblogic.WebLogic;
import io.dongtai.iast.agent.middlewarerecognition.websphere.WebSphere;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 用于检测当前运行的服务
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ServerDetect {

    // 注意，此处的注册顺序最好是要跟常见程序对应的，这样匹配的时候效率会高一些
    // 这里的顺序不要随意调整，因为匹配原理可能会导致调整之后反而识别错误，现在这样也能凑活着工作反正...
    private static final IServer[] servers = {
            new Tomcat(),
            new TomcatV9(),
            new TomcatV8(),
            new TomcatV7(),
            new TomcatV6(),
            new TomcatV5(),
            new Jetty(),
            new JBoss(),
            new JBossAS(),
            new WebSphere(),
            new WebLogic(),
            new SpringService(),
            new ServletService(),
            new DubboService(),
            new GrpcService(),
            new UnknownService()
    };

    /**
     * 获取当前使用的WebServer服务是啥
     *
     * @return
     */
    public static IServer getWebserver() {

        // TODO 没看懂这个检查是什么原因
        if (AgentLauncher.launchMode == AgentLauncher.LaunchMode.ATTACH) {
            DongTaiLog.info("io.dongtai.iast.agent.middlewarerecognition.ServerDetect.getWebserver return UnknownService, because it's in attach mode");
            return new UnknownService();
        }

        // 然后开始逐个匹配
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (IServer server : servers) {
            if (server.isMatch(runtimeMXBean, loader)) {
                return server;
            }
        }

        return null;
    }

    public static String getWebServerPath() {
        File file = new File(".");
        String path = file.getAbsolutePath();
        return path.substring(0, path.length() - 2);
    }
}