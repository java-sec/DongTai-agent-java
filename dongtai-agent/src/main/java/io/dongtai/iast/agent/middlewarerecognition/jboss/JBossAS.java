package io.dongtai.iast.agent.middlewarerecognition.jboss;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * JBoss中间件从JBoss7开始，更名为JBoss AS 7，在版本8又更名为WildFly 8
 *
 * @author dongzhiyong@huoxian.cn
 * @see JBoss
 * @see WildFly
 */
public class JBossAS implements IServer {

    public static final String NAME = "JBossAS";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        // TODO 2023-9-28 14:40:41 仅仅只是判断环境变量的话是不是有点草率了？是不是应该配合一些Class之类的更可靠一些？
        return runtimeMXBean.getSystemProperties().get("jboss.server.base.dir") != null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        // TODO 2023-9-28 14:39:53 啊？版本号感觉有点草率啊哥...
        return "7 or later";
    }

}
