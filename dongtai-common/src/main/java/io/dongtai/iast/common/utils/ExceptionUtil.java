package io.dongtai.iast.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 处理异常相关的公共逻辑提取到这里
 *
 * @author CC11001100
 * @since 1.13.2
 */
public class ExceptionUtil {

    /**
     * 把printStackTrace会打印的内容以字符串的形式返回
     * <p>
     * TODO 注意这个方法在高频调用的时候性能会有问题，可能会导致CPU占用过高，此问题暂无比较好的解决方案，还在内部讨论中...
     *
     * @param e
     * @return
     */
    public static String getPrintStackTraceString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

}
