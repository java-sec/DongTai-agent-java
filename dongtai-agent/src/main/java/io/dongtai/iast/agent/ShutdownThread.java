package io.dongtai.iast.agent;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;

/**
 * TODO 在卸载的时候是否有必要执行这么一个hook呢？JVM都要退出了还卸载干嘛？
 */
public class ShutdownThread extends Thread {

    @Override
    public void run() {
        if (!MonitorDaemonThread.isExit) {
            EngineManager.getInstance().uninstall();
        }
    }

}
