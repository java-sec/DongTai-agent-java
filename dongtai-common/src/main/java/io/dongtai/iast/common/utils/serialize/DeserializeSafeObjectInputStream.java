package io.dongtai.iast.common.utils.serialize;


import io.dongtai.iast.common.exception.DongTaiIastIllegalArgumentException;

import java.io.*;
import java.util.List;

/**
 * 反序列化安全的ObjectInputStream
 *
 * @author chenyi
 * @date 2021/9/22
 */
public class DeserializeSafeObjectInputStream extends ObjectInputStream {

    /**
     * 白名单类型列表
     */
    private final List<Class<?>> targetClazzWhiteList;

    /**
     * 类名称前缀黑名单列表
     */
    private final List<String> targetClazzPrefixBlackList;

    /**
     * 实例化反序列化安全的ObjectInputStream，需要指定黑名单或者白名单
     *
     * @param in                   InputStream 对象输入流
     * @param clazzWhiteList       类型白名单
     * @param clazzPrefixBlackList 类名前缀黑名单
     * @throws IOException IO异常
     */
    public DeserializeSafeObjectInputStream(InputStream in, List<Class<?>> clazzWhiteList, List<String> clazzPrefixBlackList) throws IOException, DongTaiIastIllegalArgumentException {
        super(in);
        this.targetClazzWhiteList = clazzWhiteList;
        this.targetClazzPrefixBlackList = clazzPrefixBlackList;
        if (isWhiteListEmpty() && isBlackListEmpty()) {
            // 噢感情您这个名单是必须配置的是吧
            String msg = this.getClass().getName() + ": The deserialization blacklist and whitelist are configured incorrectly. At least one of the whitelist and the blacklist is configured.";
            throw new DongTaiIastIllegalArgumentException(msg);
        }
    }

    /**
     * resolveClass方法hook,依据黑白名单进行反序列化阻断
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        checkWhiteList(desc);
        checkBlackList(desc);
        return super.resolveClass(desc);
    }

    /**
     * 校验类型是否在白名单列表中
     *
     * @param desc 反序列化目标类
     * @throws InvalidClassException 类型非法异常
     */
    private void checkWhiteList(ObjectStreamClass desc) throws InvalidClassException {
        if (isWhiteListEmpty()) {
            return;
        }
        String descName = desc.getName();
        boolean isInWhiteList = false;
        for (Class<?> whiteClazz : this.targetClazzWhiteList) {
            if (whiteClazz != null && whiteClazz.getName().equals(descName)) {
                isInWhiteList = true;
                break;
            }
        }
        if (!isInWhiteList) {
            throw new InvalidClassException(desc.getName(), "Unsafe deserialization, illegal class type");
        }
    }

    /**
     * 校验类型名称是否在 类名称前缀黑名单 列表中
     *
     * @param desc 反序列化目标类
     * @throws InvalidClassException 类型非法异常
     */
    private void checkBlackList(ObjectStreamClass desc) throws InvalidClassException {
        if (isBlackListEmpty()) {
            return;
        }
        String descName = desc.getName();
        if (descName == null) {
            return;
        }
        for (String blackClazz : this.targetClazzPrefixBlackList) {
            // 检查类型namespace前缀
            if (blackClazz != null && descName.startsWith(blackClazz)) {
                throw new InvalidClassException(desc.getName(), "Unsafe deserialization, class type is illegal!");
            }
        }
    }

    private boolean isWhiteListEmpty() {
        return targetClazzWhiteList == null || targetClazzWhiteList.isEmpty();
    }

    private boolean isBlackListEmpty() {
        return targetClazzPrefixBlackList == null || targetClazzPrefixBlackList.isEmpty();
    }

}
