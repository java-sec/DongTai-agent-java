package io.dongtai.iast.common.utils.serialize;

import io.dongtai.iast.common.exception.DongTaiIastIllegalArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 序列化/反序列化工具类
 *
 * @author chenyi
 * @date 2022/3/1
 */
public class SerializeUtils {

    private static final String DEFAULT_CHARSET = "ISO-8859-1";

    // 默认允许反序列化的类型，都是内置的一些类型，认为这些类型是安全的
    private static final List<Class<?>> DEFAULT_SAFE_CLASSES = new ArrayList<Class<?>>() {
        private static final long serialVersionUID = -2140605358789870025L;

        {
            add(java.lang.Boolean.class);
            add(java.lang.Byte.class);
            add(java.lang.Short.class);
            add(java.lang.Character.class);
            add(java.lang.Integer.class);
            add(java.lang.Long.class);
            add(java.lang.Float.class);
            add(java.lang.Double.class);
            add(java.lang.Number.class);
            add(java.lang.String.class);
            add(java.lang.Enum.class);
            add(java.util.ArrayList.class);
            add(java.util.Date.class);
        }
    };

    private SerializeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 序列化对象
     */
    public static String serializeByObject(Serializable obj) throws Exception {
        return serialize(obj);
    }

    /**
     * 序列化列表对象
     */
    public static String serializeByList(List<?> objList) throws Exception {
        return serialize(objList);
    }

    /**
     * 反序列化对象
     */
    public static <T extends Serializable> T deserialize2Object(String str, Class<T> type, List<Class<?>> clazzWhiteList)
            throws IOException, ClassNotFoundException, DongTaiIastIllegalArgumentException {
        ObjectInputStream objIn = deserialize(str, clazzWhiteList);
        return type.cast(objIn.readObject());
    }

    /**
     * 反序列化列表对象
     *
     * @param objectStreamString
     * @param clazzWhiteList
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws DongTaiIastIllegalArgumentException
     */
    public static <T extends Serializable> ArrayList<T> deserialize2ArrayList(String objectStreamString, List<Class<?>> clazzWhiteList)
            throws IOException, ClassNotFoundException, DongTaiIastIllegalArgumentException {
        ObjectInputStream objIn = deserialize(objectStreamString, clazzWhiteList);
        return new ArrayList<T>().getClass().cast(objIn.readObject());
    }

    /**
     * 把对象序列化为对象流字符串
     *
     * @param obj 要被序列化的对象
     * @return
     * @throws IOException
     */
    private static String serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(obj);
        return byteOut.toString(DEFAULT_CHARSET);
    }

    /**
     * 把 反序列化的对象流字符串反序列化为对象流
     *
     * @param objectStreamString 类型反序列化后的对象流字符串
     * @param clazzWhiteList     允许的类型白名单
     * @return
     * @throws IOException
     * @throws DongTaiIastIllegalArgumentException
     */
    private static ObjectInputStream deserialize(String objectStreamString, List<Class<?>> clazzWhiteList) throws IOException, DongTaiIastIllegalArgumentException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(objectStreamString.getBytes(DEFAULT_CHARSET));
        List<Class<?>> targetClazzWhiteList = new ArrayList<Class<?>>(clazzWhiteList);
        targetClazzWhiteList.addAll(DEFAULT_SAFE_CLASSES);
        return new DeserializeSafeObjectInputStream(byteIn, targetClazzWhiteList, null);
    }
}
