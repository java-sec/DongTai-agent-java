package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.impl.*;
import io.dongtai.iast.common.utils.version.JavaVersionUtils;
import io.dongtai.log.DongTaiLog;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {

    // 单例对象
    private static MonitorDaemonThread instance;

    // 当前注册上来的监控任务
    public static ArrayList<IMonitor> monitorTasks;

    // 用于标志monitor是否退出
    // TODO 但是似乎并没有地方修改它的值？所以这是一个废弃变量？
    public static boolean isExit = false;

    private final EngineManager engineManager;

    // 延时启动的时间，单位为秒
    public static int delayTime = 0;

    // 引擎是否启动成功
    public static boolean engineStartSuccess = false;

    public MonitorDaemonThread(EngineManager engineManager) {
        this.engineManager = engineManager;

        // 启动的监控任务，要新增的话就在这里注册
        monitorTasks = new ArrayList<IMonitor>();
        monitorTasks.add(new FallbackConfigMonitor());
        monitorTasks.add(new ConfigMonitor());
        monitorTasks.add(new PerformanceMonitor());
        monitorTasks.add(new AgentStateMonitor(engineManager));
        monitorTasks.add(new HeartBeatMonitor());

        // 延时启动，用于应用一次添加了多个agent的时候礼让同行
        try {
            delayTime = IastProperties.getInstance().getDelayTime();
            DongTaiLog.debug("dongtai engine delay time is {} seconds.", delayTime);
            if (delayTime > 0) {
                delayTime = delayTime * 1000;
            }
        } catch (Throwable e) {
            // 这种配置错误应该被暴露出来而不是掩盖掉
            DongTaiLog.error("dongtai engine delay time must be int, eg: 15", e);
            delayTime = 0;
        }
    }

    // TODO lazy单例DCL
    public static MonitorDaemonThread getInstance(EngineManager engineManager) {
        if (instance == null) {
            instance = new MonitorDaemonThread(engineManager);
        }
        return instance;
    }

    @Override
    public void run() {

        // 处理延迟启动的休眠时间
        if (delayTime > 0) {
            // 休眠时打印debug日志方便方便用户观察启动的时候是卡在哪里了
            DongTaiLog.debug("set the delay time and start sleeping for {} second", delayTime);
            try {
                Thread.sleep(delayTime);
                DongTaiLog.debug("delay time sleep done, I woke up!");

                startEngine();
            } catch (InterruptedException e) {
                DongTaiLog.error("delay time sleep exception", e);
            }
        } else {
            DongTaiLog.debug("delay time is {}, so ignored it.", delayTime);
        }

        // 引擎启动成功后，创建子线程执行monitor任务
        if (engineStartSuccess) {
            for (IMonitor monitor : monitorTasks) {
                Thread monitorThread = new Thread(monitor, monitor.getName());
                monitorThread.setDaemon(true);
                monitorThread.setPriority(1);
                monitorThread.start();
                DongTaiLog.debug("start monitor thread {}", monitor.getName());
            }
        }
    }

    //todo: 检测所有线程信息。

    /**
     * 启动监控引擎，运行注册的所有的IMonitor
     */
    public void startEngine() {
        boolean status = false;
        if (couldInstallEngine()) {
            // jdk8以上
            status = this.engineManager.extractOrDownloadDongTaiJar() && engineManager.install();
        }
        if (!status) {
            DongTaiLog.error("DongTai IAST started failure");
        }
        engineStartSuccess = status;
    }

    /**
     * 是否可以安装引擎，要求最低的JDK版本为1.8
     *
     * @return boolean
     */
    private boolean couldInstallEngine() {
        // TODO 2023-9-22 11:22:38 这里的判断逻辑是否合理呢？会不会出现更低的版本？万一出现了会怎样？
        // 低版本jdk暂不支持安装引擎core包
        if (JavaVersionUtils.isJava6() || JavaVersionUtils.isJava7()) {
            DongTaiLog.warn("DongTai Engine core couldn't install because of your low JDK version {}, The minimum supported JDK version is 1.8", JavaVersionUtils.javaVersionStr());
            return false;
        }
        return true;
    }

}
