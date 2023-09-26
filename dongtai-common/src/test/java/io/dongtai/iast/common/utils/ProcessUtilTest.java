package io.dongtai.iast.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author CC11001100
 */
public class ProcessUtilTest {

    @Test
    public void getPid() {
        long pid = ProcessUtil.getPid();
        Assert.assertNotEquals(0, pid);
    }
    
}