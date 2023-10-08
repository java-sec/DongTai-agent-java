package io.dongtai.iast.agent.middlewarerecognition.weblogic;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.io.File;
import java.lang.management.RuntimeMXBean;

/**
 * 识别web logic服务
 *
 * @author dongzhiyong@huoxian.cn
 * @author CC11001100
 */
public class WebLogic implements IServer {

    public static final String NAME = "WebLogic";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        File runFile = new File(".", "bin/startWebLogic.sh");
        File configFile = new File(".", "init-info/domain-info.xml");
        System.setProperty("UseSunHttpHandler", "true");
        return runFile.exists() && configFile.exists();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        // TODO 从xml中解析版本，这里直接返回显然是不对的，先直接置为null
//        return "WebLogic";
        return null;
    }

}
