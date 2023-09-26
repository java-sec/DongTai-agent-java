package io.dongtai.iast.common.utils;

import io.dongtai.log.DongTaiLog;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 进程相关的工具类
 *
 * @author CC11001100
 * @since v1.15.0
 */
public class ProcessUtil {

    // 此处这个默认的pid 0是为了兼容服务端的逻辑，服务端取值不能为空，那没有取到的时候就必须指定一个值，那能怎么办呢，所以就约定为0了
    // see #IAST-485
    public static final long DEFAULT_PID = 0;

    /**
     * 获取当前jvm进程的pid
     * <p>
     * <p>
     * tips: 注意，因为兼容性的问题，内部实现会优先使用反射，所以在高并发场景下情自行cache
     *
     * @return
     */
    public static long getPid() {

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // 1. 先尝试通过反射获取pid
        try {
            // jdk 10的时候RuntimeMXBean增加了一个getPid()的方法，先尝试看看能不能通过这个方法获取到pid，毕竟讲道理这个方法应该会更准一些...吧？
            // long getPid()
            // 因为我们的编译环境是jdk 1.8，所以这里为了兼容性能考虑就使用反射了
            return (long) runtimeMXBean.getClass().getMethod("getPid").invoke(runtimeMXBean);
        } catch (Throwable e) {
            DongTaiLog.trace("ManagementFactory.getRuntimeMXBean().getPid() throw : {}", e.getMessage());
        }

        // 2. 我草不行了JVM里找不到那个直接获取的方法，那只好自己解析了
        String processName = runtimeMXBean.getName();
        Matcher matcher = Pattern.compile("^\\d+").matcher(processName);
        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        } else {
            DongTaiLog.warn("get pid parsing process name exception, process name is {}", processName);
        }

        return DEFAULT_PID;
    }

}
