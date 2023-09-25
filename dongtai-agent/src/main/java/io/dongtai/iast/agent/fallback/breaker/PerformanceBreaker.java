package io.dongtai.iast.agent.fallback.breaker;

import io.dongtai.iast.agent.fallback.FallbackConfig;
import io.dongtai.iast.agent.fallback.FallbackSwitch;
import io.dongtai.iast.agent.fallback.checker.IPerformanceChecker;
import io.dongtai.iast.agent.fallback.checker.MetricsBindCheckerEnum;
import io.dongtai.iast.agent.util.GsonUtils;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.common.state.State;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 性能熔断器实现(仅支持JDK8+)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class PerformanceBreaker extends AbstractBreaker {

    // 单例对象
    private static PerformanceBreaker instance;

    // 熔断器
    private static CircuitBreaker breaker;

    // 用户使用的洞态java agent配置文件，也没啥封装，直接传到这里来了Properties...
    private static Properties cfg;

    /**
     * 因为熔断器需要从配置中获取一些参数，所以创建的时候需要把配置的properties传进来
     *
     * @param cfg
     */
    private PerformanceBreaker(Properties cfg) {
        super(cfg);
    }

    /**
     * 懒汉式单例方法
     *
     * @param cfg
     * @return
     */
    public static PerformanceBreaker newInstance(Properties cfg) {
        if (instance == null) {
            instance = new PerformanceBreaker(cfg);
        }
        return instance;
    }

    @Override
    public void breakCheck(String contextString) {

        // 检查器未初始化的话则跳过检查
        if (breaker == null) {
            DongTaiLog.info("the performance breaker need to be init, skip check.");
            return;
        }

        // 未开启熔断的话则跳过检查
        if (!FallbackConfig.enableAutoFallback()) {
            DongTaiLog.trace("the performance breaker not enable, skip check.");
            return;
        }

        // agent安装的时候发生了异常根本就没有安装成功，所以跳过检查
        // 这里之所以没有直接退出，应该是因为可能会出现重新安装的话安装成功的情况
        if (AgentState.getInstance().isException()) {
            DongTaiLog.trace("the performance breaker find agent is install exception, skip check.");
            return;
        }

        // 然后才开始真正的开启检查
        Try.ofSupplier(CircuitBreaker.decorateSupplier(breaker, () -> checkMetricsWithAutoFallback(contextString)))
                .recover(throwable -> {
                    if (throwable instanceof CallNotPermittedException) {
                        return false;
                    }
                    DongTaiLog.info("the performance breaker checker throw exception: {}", throwable.getMessage());
                    return false;
                }).get();
    }

    /**
     * 检查当前的负载是否触发熔断
     *
     * @param contextString
     * @return
     */
    private static boolean checkMetricsWithAutoFallback(String contextString) {

        List<PerformanceMetrics> performanceMetrics = convert2MetricsList(contextString);

        // 检查每个性能是否达到限制值
        for (PerformanceMetrics metrics : performanceMetrics) {
            final IPerformanceChecker performanceChecker = MetricsBindCheckerEnum.newCheckerInstance(metrics.getMetricsKey());
            if (performanceChecker != null && performanceChecker.isPerformanceOverLimit(metrics, cfg)) {
                final PerformanceMetrics threshold = performanceChecker.getMatchMaxThreshold(metrics.getMetricsKey(), cfg);
                throw new IllegalStateException("performance is over max threshold! metrics:" + GsonUtils.toJson(metrics));
            }
        }
        return true;
    }

    /**
     * 将上下文转换为指标列表，就是把参数反序列化
     *
     * @param contextString 上下文字符串
     * @return {@link List}<{@link PerformanceMetrics}> 指标列表
     */
    private static List<PerformanceMetrics> convert2MetricsList(String contextString) {
        // 这个参数传递确实有点太草了...
        // TODO 2023-9-25 14:24:28 调查要这样传递参数的原因并重构
        try {
            final List<Class<?>> clazzWhiteList = Arrays.asList(
                    PerformanceMetrics.class,
                    MetricsKey.class,
                    CpuInfoMetrics.class,
                    MemoryUsageMetrics.class);
            return SerializeUtils.deserialize2ArrayList(contextString, clazzWhiteList);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.AGENT_FALLBACK_BREAKER_CONVERT_METRICS_FAILED, e.getMessage());
            return new ArrayList<PerformanceMetrics>();
        }
    }

    @Override
    protected void initBreaker(Properties cfg) {
        PerformanceBreaker.cfg = cfg;
        final Integer breakerWindowSize = FallbackConfig.getPerformanceBreakerWindowSize(cfg);
        final Double breakerFailureRate = FallbackConfig.getPerformanceBreakerFailureRate(cfg);
        final Integer breakerWaitDuration = FallbackConfig.getPerformanceBreakerWaitDuration(cfg);
        // 创建断路器自定义配置
        CircuitBreaker breaker = CircuitBreaker.of("iastPerformanceBreaker", CircuitBreakerConfig.custom()
                // 基于次数的滑动窗口(默认窗口大小2)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(breakerWindowSize)
                //失败率阈值百分比(默认>=51%)
                .failureRateThreshold(breakerFailureRate.floatValue())
                //计算失败率或慢调用率之前所需的最小调用数
                .minimumNumberOfCalls(breakerWindowSize)
                //自动从开启变成半开(默认等待40秒)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(breakerWaitDuration))
                // 半开时允许通过次数(默认窗口大小*5)
                .permittedNumberOfCallsInHalfOpenState(breakerWindowSize * 5)
                // 关注的失败异常类型
                .recordExceptions(IllegalStateException.class)
                .build());
        // 断路器事件监听
        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    if (AgentState.getInstance().getState() == null) {
                        return;
                    }
                    final CircuitBreaker.State fromState = event.getStateTransition().getFromState();
                    final CircuitBreaker.State toState = event.getStateTransition().getToState();
                    if (toState == CircuitBreaker.State.OPEN) {
                        if (fromState == CircuitBreaker.State.CLOSED) {
                            FallbackSwitch.setPerformanceFallback(State.PAUSED);
                        } else {
                            if (!AgentState.getInstance().isUninstalled()) {
                                FallbackSwitch.setPerformanceFallback(State.UNINSTALLED);
                            }
                        }
                    } else if (toState == CircuitBreaker.State.HALF_OPEN) {
                        if (!AgentState.getInstance().isUninstalled() && !AgentState.getInstance().isPaused()) {
                            FallbackSwitch.setPerformanceFallback(State.PAUSED);
                        }
                    } else if (toState == CircuitBreaker.State.CLOSED) {
                        FallbackSwitch.setPerformanceFallback(State.RUNNING);
                    }
                });
        PerformanceBreaker.breaker = breaker;
    }

}
