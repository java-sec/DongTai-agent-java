package io.dongtai.iast.agent.middlewarerecognition.gRPC;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 * @author CC11001100
 */
public class GrpcService implements IServer {

    public static final String NAME = "gRPC";

    @Override
    public boolean isMatch(RuntimeMXBean runtimeMXBean, ClassLoader loader) {
        try {
            loader.loadClass("io.grpc.internal.ServerImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return null;
    }

}
