package io.dongtai.iast.agent.manager;

import io.dongtai.iast.agent.IastClassLoader;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.LogCollector;
import io.dongtai.iast.agent.fallback.FallbackManager;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.common.utils.ProcessUtil;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * 引擎管理类，负责engine模块的完整生命周期，包括：下载、安装、启动、停止、重启、卸载
 *
 * @author dongzhiyong@huoxian.cn
 * @author CC11001100
 */
public class EngineManager {

    // core包中的引擎管理器的全路径类名
    private static final String ENGINE_ENTRYPOINT_CLASS = "io.dongtai.iast.core.AgentEngine";

    // 上面那个类的Class
    private Class<?> classOfEngine;

    // 从Server拉取jar包的几个API路径
    private static final String INJECT_PACKAGE_REMOTE_URI = "/api/v1/engine/download?engineName=dongtai-spy";
    private static final String ENGINE_PACKAGE_REMOTE_URI = "/api/v1/engine/download?engineName=dongtai-core";
    private static final String API_PACKAGE_REMOTE_URI = "/api/v1/engine/download?engineName=dongtai-api";

    // 临时目录？其实更应该叫做workspace吧...先这么凑活叫吧后面需要的时候再修改
    private final static String workspace = IastProperties.getInstance().getWorkspace();

    // 洞态引擎自己使用的类加载器
    private static IastClassLoader iastClassLoader;

    // 单例模式
    private static EngineManager instance;

    // 当前进程的pid
    private static final long pid = ProcessUtil.getPid();

    private final Instrumentation inst;

    // 配置文件相关
    private final IastProperties properties;

    // agent的启动模式，是-javaagent参数启动的还是attach附加的
    private final String launchMode;

    // 熔断管理器，我草这个为啥在这里...
    private final FallbackManager fallbackManager;

    // agent的当前状态
    private final AgentState agentState;

    /**
     * 获取IAST引擎管理器的单例对象
     *
     * @param inst       instrumentation接口实例化对象
     * @param launchMode IAST引擎的启动模式，attach、premain两种
     * @param ppid       IAST引擎运行的进程ID，用于后续进行热更新
     * @return IAST引擎管理器的实例化对象
     */
    public static EngineManager getInstance(Instrumentation inst, String launchMode, String ppid, AgentState agentState) {
        // TODO 这个类的单例似乎应该搞得稍微合理一点，不然感觉有点奇怪
        if (instance == null) {
            instance = new EngineManager(inst, launchMode, ppid, agentState);
        }
        return instance;
    }

    /**
     * 获取IAST引擎管理器的单例对象
     *
     * @return IAST引擎管理器的实例化对象
     */
    public static EngineManager getInstance() {
        return instance;
    }

    public static FallbackManager getFallbackManager() {
        return instance.fallbackManager;
    }

    public EngineManager(Instrumentation inst, String launchMode, String ppid, AgentState agentState) {
        this.inst = inst;
        this.launchMode = launchMode;
        this.properties = IastProperties.getInstance();
        this.fallbackManager = FallbackManager.newInstance(this.properties.cfg);
        this.agentState = agentState;
    }

    /**
     * 获取IAST检测引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return engine包的本地保存路径
     */
    private static String getEnginePackageCachePath() {
        return workspace + "dongtai-core.jar";
    }

    /**
     * 获取IAST间谍引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return inject包的本地路径
     */
    private static String getInjectPackageCachePath() {
        return workspace + "dongtai-spy.jar";
    }

    /**
     * 获取IAST间谍引擎本地保存的临时路径，用于后续从本地目录加载Jar包
     *
     * @return inject包的本地路径
     */
    private static String getApiPackagePath() {
        return workspace + "dongtai-api.jar";
    }

    private static String getGrpcPackagePath() {
        return workspace + "dongtai-grpc.jar";
    }

    /**
     * 更新IAST引擎需要的jar包，用于启动时加载和热更新检测引擎 - iast-core.jar - iast-inject.jar
     *
     * @return 更新状态，成功为true，失败为false
     */
    public boolean downloadPackageFromServer() {
        // 自定义jar下载地址
        DongTaiLog.debug("start downloading the jar package from the server...");
        String spyJarUrl = "".equals(properties.getCustomSpyJarUrl()) ? INJECT_PACKAGE_REMOTE_URI : properties.getCustomSpyJarUrl();
        String coreJarUrl = "".equals(properties.getCustomCoreJarUrl()) ? ENGINE_PACKAGE_REMOTE_URI : properties.getCustomCoreJarUrl();
        String apiJarUrl = "".equals(properties.getCustomApiJarUrl()) ? API_PACKAGE_REMOTE_URI : properties.getCustomApiJarUrl();
        return HttpClientUtils.downloadRemoteJar(spyJarUrl, getInjectPackageCachePath()) &&
                HttpClientUtils.downloadRemoteJar(coreJarUrl, getEnginePackageCachePath()) &&
                HttpClientUtils.downloadRemoteJar(apiJarUrl, getApiPackagePath()) &&
                HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-grpc", getGrpcPackagePath());
    }

    /**
     * 从 dongtai-agent.jar 提取相关的jar包
     *
     * @return 提取结果，成功为true，失败为false
     */
    public boolean extractPackageFromAgent() {
        DongTaiLog.debug("start unpacking jar files to local...");
        try {
            return FileUtils.getResourceToFile("bin/dongtai-spy.jar", getInjectPackageCachePath()) &&
                    FileUtils.getResourceToFile("bin/dongtai-core.jar", getEnginePackageCachePath()) &&
                    FileUtils.getResourceToFile("bin/dongtai-api.jar", getApiPackagePath()) &&
                    FileUtils.getResourceToFile("bin/dongtai-grpc.jar", getGrpcPackagePath());
        } catch (IOException e) {
            DongTaiLog.error(ErrorCode.AGENT_EXTRACT_PACKAGES_FAILED, e);
        }
        return false;
    }

    /**
     * 从agent内解压jar或者从远端下载
     * <p>
     * 这里的初始化jar包的时候需要注意，agent使用的版本需要能够与Server匹配，否则可能会导致启动失败无法正确与Server交互
     *
     * @return
     */
    public boolean extractOrDownloadDongTaiJar() {
        // 解析jar包到本地
        String spyPackage = getInjectPackageCachePath();
        String enginePackage = getEnginePackageCachePath();
        String apiPackage = getApiPackagePath();
        if (properties.isDebug()) {
            DongTaiLog.debug("current mode: debug, try to read package from directory {}", workspace);
            if ((new File(spyPackage)).exists() && (new File(enginePackage)).exists() && (new File(apiPackage)).exists()) {
                DongTaiLog.debug("find jar in directory {} success ", workspace);
                return true;
            }
        }
        if ("true".equalsIgnoreCase(properties.getIsDownloadPackage())) {
            return downloadPackageFromServer();
        } else {
            return extractPackageFromAgent();
        }
    }

    /**
     * 从本地workspace下的jar包安装洞态的引擎
     *
     * @return
     */
    public boolean install() {

        DongTaiLog.debug("DongTai engine begin install...");

        String spyPackage = EngineManager.getInjectPackageCachePath();
        String corePackage = EngineManager.getEnginePackageCachePath();
        try {

            // 把spy加到bootstrap，要不然某些类会找不到
            JarFile file = new JarFile(new File(spyPackage));
            inst.appendToBootstrapClassLoaderSearch(file);
            file.close();

            if (iastClassLoader == null) {
                iastClassLoader = new IastClassLoader(corePackage);
            }

            // 反射调用core包中的AgentEngine，开始安装引擎...
            classOfEngine = iastClassLoader.loadClass(ENGINE_ENTRYPOINT_CLASS);
            String agentPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            classOfEngine.getMethod("install", String.class, String.class, Integer.class, Instrumentation.class, String.class)
                    .invoke(null, launchMode, this.properties.getPropertiesFilePath(), AgentRegisterReport.getAgentId(), inst, agentPath);

            DongTaiLog.debug("DongTai engine install done.");

            return true;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_INSTALL_FAILED, e);
        }
        return false;
    }

    /**
     * 启动检测引擎
     */
    public boolean start() {
        try {
            // 这个似乎是再次拉起来的时候的
            if (classOfEngine != null) {
                classOfEngine.getMethod("start").invoke(null);
                DongTaiLog.info("DongTai engine start successfully.");
                return true;
            } else {
                DongTaiLog.info("DongTai engine class is null");
                return false;
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_START_FAILED, e);
        }
        return false;
    }

    /**
     * 停止检测引擎
     *
     * @return 布尔值，表示stop成功或失败
     */
    public boolean stop() {
        try {
            if (classOfEngine != null) {
                classOfEngine.getMethod("stop").invoke(null);
                DongTaiLog.info("DongTai engine stop successfully.");
                return true;
            } else {
                DongTaiLog.info("DongTai engine class is null");
                return false;
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_STOP_FAILED, e);
        }
        return false;
    }

    /**
     * 卸载间谍包、检测引擎包
     *
     * @question: 通过 inst.appendToBootstrapClassLoaderSearch() 方法加入的jar包无法直接卸载；
     */
    public synchronized boolean uninstall() {
        try {
            // TODO: state
            if (null == iastClassLoader) {
                return true;
            }

            if (classOfEngine != null) {
                classOfEngine.getMethod("destroy", String.class, String.class, Instrumentation.class)
                        .invoke(null, launchMode, this.properties.getPropertiesFilePath(), inst);
            }

            // 关闭SandboxClassLoader
            // class被释放的三个条件：
            // 1. 此Class的所有对象实例均被销毁
            // 2. 没有其他地方引用此Class
            // 3. 加载此Class的ClassLoader被销毁
            classOfEngine = null;
            iastClassLoader.closeIfPossible();
            iastClassLoader = null;
            LogCollector.stopFluent();
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_REFLECTION_UNINSTALL_FAILED, e);
        } finally {
            // 有点狠了哥
            ThreadUtils.killAllDongTaiCoreThreads();
        }
        return true;
    }

    /**
     * 返回agent的当前状态
     *
     * @return
     */
    public AgentState getAgentState() {
        return this.agentState;
    }

    /**
     * 获取当前的pid进程号
     *
     * @return
     */
    public static long getPid() {
        return pid;
    }

}
