package io.dongtai.iast.agent;

import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;

import static io.dongtai.iast.agent.Agent.*;

/**
 * 用于把Agent的日志收集到Server端
 *
 * @author lostsnow
 * @author CC11001100
 */
public class LogCollector {

    // fluent可执行文件存放的绝对路径
    private static String fluentFile;

    // fluent配置文件存放的绝对路径
    private static String fluentFileConf;

    // fluent启动的进程
    private static Process fluent;

    // 关闭fluent的shutdown hook
    private static Thread shutdownHook;

    /**
     * 解压并启动fluent进程收集
     */
    public static void extractAndStartFluent() {
        if (extractFluent()) {
            doFluent();
        }
    }

    /**
     * 解压jar包中带的fluent文件到本地系统
     */
    private static boolean extractFluent() {
        try {

            // 如果没有在配置文件中开启日志收集的话则不启动日志
            // TODO 但是此处的日志目录为空就不启动收集了？这是个什么鬼逻辑是为啥啊万一人家还没开始等会儿就开始打了呢...
            if (IastProperties.getInstance().getLogDisableCollector() || DongTaiLog.getLogPath().isEmpty()) {
                DongTaiLog.info("fluent log collection is not enabled and related threads are no longer started");
                return false;
            }

            // TODO 目前仅支持在Linux下收集日志
            if (isMacOs() || isWindows()) {
                DongTaiLog.info("only supports log collection under Linux, and the log collection service will no longer be started");
                return false;
            }

            fluentFileConf = IastProperties.getInstance().getWorkspace() + "fluent-" + AgentRegisterReport.getInstance().getAgentIdAsString() + ".conf";
            FileUtils.getResourceToFile("bin/fluent.conf", fluentFileConf);
            FileUtils.confReplace(fluentFileConf);

            String multiParserFile = IastProperties.getInstance().getWorkspace() + "parsers_multiline.conf";
            FileUtils.getResourceToFile("bin/parsers_multiline.conf", multiParserFile);
            FileUtils.confReplace(multiParserFile);

            fluentFile = IastProperties.getInstance().getWorkspace() + "fluent";
            File f = new File(fluentFile);
            if (f.exists()) {
                // 会有版本升级的问题吗？我们应该轻易不会升级版本吧，升级的时候再说
                DongTaiLog.debug("the fluent workspace {} already exists, no need to extract it again", fluentFile);
                return true;
            }
            if (isArm()) {
                FileUtils.getResourceToFile("bin/fluent-arm", fluentFile);
            } else {
                FileUtils.getResourceToFile("bin/fluent", fluentFile);
            }

            if (!(new File(fluentFile)).setExecutable(true)) {
                DongTaiLog.warn(ErrorCode.FLUENT_SET_EXECUTABLE_FAILED, fluentFile);
            }

            return true;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.FLUENT_EXTRACT_FAILED, e);
        }
        return false;
    }

    /**
     * 启动
     */
    private static void doFluent() {
        // TODO 不需要判断是否已经启动过了吗？会不会造成同时启动多个实例？比如先卸载，再安装，再卸载，再安装？
        String[] execution = {
                "nohup",
                fluentFile,
                "-c",
                fluentFileConf
        };
        try {
            fluent = Runtime.getRuntime().exec(execution);
            DongTaiLog.info("fluent process started");
            shutdownHook = new Thread(new Runnable() {
                @Override
                public void run() {
                    stopFluent();
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.FLUENT_PROCESS_START_FAILED, e);
        }
    }

    /**
     * 停止fluent收集日志，一般在agent卸载的时候调用
     */
    public static void stopFluent() {

        if (fluent == null) {
            DongTaiLog.info("fluent has stopped and does not need to be stopped again");
            return;
        }

        try {
            fluent.destroy();
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            DongTaiLog.info("fluent process stopped");
        } catch (Throwable e) {
            DongTaiLog.error("exception occurred while stopping fluent process: {}", e.getMessage());
        } finally {
            fluent = null;
            shutdownHook = null;
        }
    }

}
