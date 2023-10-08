package io.dongtai.iast.agent.middlewarerecognition;

import java.lang.management.RuntimeMXBean;

/**
 * 服务探测器，用于探测当前JVM进程中是否有对应服务存在
 *
 * @author dongzhiyong@huoxian.cn
 * @author CC11001100
 */
public interface IServer {

    /**
     * 当前JVM中是否包含这个服务
     * <p>
     * tips: 这个匹配的判断应该尽量准确，因为使用的时候是一个IServer列表依次进行短路匹配，
     * 如果是不正确的匹配会导致后续被break从而匹配到错误的结果
     *
     * @param runtimeMXBean jmx manage bean
     * @param loader        当前的ClassLoader
     * @return 匹配的话返回true，否则返回false
     */
    boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader);

    /**
     * 识别到的服务的名称
     *
     * @return 如果未识别到服务名称，默认应该返回null
     */
    String getName();

    /**
     * 识别到的服务的版本号
     *
     * @return 如果未识别到版本，默认应该返回null
     */
    String getVersion();

}
