package io.dongtai.iast.agent.middlewarerecognition;


/**
 * 用于检测当前的ClassLoader是否能加载给定的类，后面会有一些服务识别通过是否存在这个Class来判断是否有对应的服务
 * <p>
 * TODO 调研一下这样子搞的准确度，会不会出现仅仅只是依赖里有相关Class但是实际上并没有被调用，然后就误判了...
 *
 * @author dongzhiyong@huoxian.cn
 */
public final class PackageManager {

    // 要识别的类名
    private final String classname;

    public PackageManager(String classname) {
        this.classname = classname;
    }

    public Package getPackage() {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(this.classname);
            return clazz.getPackage();
        } catch (Throwable e) {
            return null;
        }
    }

}

