package io.dongtai.iast.agent.middlewarerecognition.glassfish;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;
import java.net.URL;

/**
 * TODO 2023-9-28 14:26:52 不知道什么原因没有开启对这个Web容器的支持，调研一下同时确认是否需要继续增加对此容器的支持
 *
 * @author dongzhiyong@huoxian.cn
 * @see GlassFish4
 */
@Deprecated
public class GlassFish3 implements IServer {

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        try {
            URL url = loader.getResource(" org/glassfish/grizzly/http/server/HttpServer.class".substring(1));
            return url != null;
        } catch (Throwable ignore) {
            return false;
        }
    }

    @Override
    public String getName() {
        // TODO 2023-9-28 14:22:27 这个名字其实应该是 "Grizzly" ？
        return "grizzly";
    }

    @Override
    public String getVersion() {
        // TODO 2023-9-28 14:22:19 此处的版本号对吗？为啥是返回了一个名字，需要与Server端开发确认是否是有什么历史遗留原因
        return "Grizzly";
    }

}
