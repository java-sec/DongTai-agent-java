package io.dongtai.iast.agent.util;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.*;
import java.net.URI;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 从jar包中抽取资源到指定的路径
     *
     * @param resourceName 要抽取的文件在jar中的位置
     * @param fileName     要解压到的位置
     * @return 是否抽取成功
     * @throws IOException
     */
    public static boolean getResourceToFile(String resourceName, String fileName) throws IOException {
        File targetFile = new File(fileName);

        if (!targetFile.exists()) {
            if (!targetFile.getParentFile().exists()) {
                if (!targetFile.getParentFile().mkdirs()) {
                    DongTaiLog.error(ErrorCode.AGENT_GET_RESOURCE_TO_FILE_FAILED, resourceName, fileName, "mkdirs");
                }
            }
            if (!targetFile.createNewFile()) {
                DongTaiLog.error(ErrorCode.AGENT_GET_RESOURCE_TO_FILE_FAILED, resourceName, fileName, "createNewFile");
            }
        }

        try {
            InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                return false;
            }
            FileOutputStream fos = new FileOutputStream(targetFile);
            int length = 0;
            byte[] data = new byte[1024];
            while ((length = is.read(data)) != -1) {
                fos.write(data, 0, length);
            }
            data = null;
            is.close();
            fos.close();
            return true;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_GET_RESOURCE_TO_FILE_FAILED, resourceName, fileName, "write", e);
            return false;
        }
    }

    /**
     * 渲染配置文件，配置文件中可以使用${name}的方式指定一些变量占位符，然后传给这个方法，此方法会渲染变量的值做实际替换
     *
     * @param path 要渲染的配置文件的位置
     */
    public static void confReplace(String path) {
        String temp = "";

        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            // 保存该行前面的内容
            while ((temp = br.readLine()) != null) {
                if (temp.contains("${HOSTNAME_AGENT_ID}")) {
                    temp = temp.replace("${HOSTNAME_AGENT_ID}", IastProperties.getInstance().getInternalHostName() + "-" + AgentRegisterReport.getInstance().getAgentIdAsString());
                } else if (temp.contains("${HOSTNAME}")) {
                    temp = temp.replace("${HOSTNAME}", IastProperties.getInstance().getInternalHostName());
                } else if (temp.contains("${AGENT_ID}")) {
                    temp = temp.replace("${AGENT_ID}", AgentRegisterReport.getInstance().getAgentIdAsString());
                } else if (temp.contains("${OPENAPI}")) {
                    String logAddress = IastProperties.getInstance().getLogAddress();
                    if (null == logAddress || logAddress.isEmpty()) {
                        String s = IastProperties.getInstance().getBaseUrl();
                        try {
                            String openApiDomain = new URI(s).getHost();
                            temp = temp.replace("${OPENAPI}", openApiDomain);
                        } catch (Throwable e) {
                            s = s.substring(s.indexOf("://") + 3, s.indexOf("/openapi"));
                            if (s.contains(":")) {
                                s = s.substring(0, s.indexOf(":"));
                            }
                            temp = temp.replace("${OPENAPI}", s);
                        }
                    } else {
                        temp = temp.replace("${OPENAPI}", logAddress);
                    }
                } else if (temp.contains("${LOG_PORT}")) {
                    String logPort = IastProperties.getInstance().getLogPort();
                    if (null == logPort || logPort.isEmpty()) {
                        String s = IastProperties.getInstance().getBaseUrl();
                        try {
                            int openApiPort = new URI(s).getPort();
                            temp = temp.replace("${LOG_PORT}", openApiPort > 0 ? Integer.toString(openApiPort) : "80");
                        } catch (Throwable e) {
                            s = s.substring(s.indexOf("://") + 3, s.indexOf("/openapi"));
                            if (s.contains(":")) {
                                s = s.substring(s.indexOf(":") + 1);
                                temp = temp.replace("${LOG_PORT}", s);
                            } else {
                                temp = temp.replace("${LOG_PORT}", "80");
                            }
                        }
                    } else {
                        temp = temp.replace("${LOG_PORT}", logPort);
                    }
                } else if (temp.contains("${LOG_PATH}")) {
                    temp = temp.replace("${LOG_PATH}", DongTaiLog.getLogPath());
                }
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }
            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭资源，忽略可能会发生的异常
     *
     * @param closeable
     */
    public static void closeIgnoreException(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable ignored) {
        }
    }

}
