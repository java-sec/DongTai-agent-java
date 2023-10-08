package io.dongtai.iast.common.string;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试StringUtil中的方法
 *
 * @see StringUtils
 */
public class StringUtilsTest {

    @Test
    public void testNormalize() {
        int maxLength = 1024;
        String str;
        String nStr;
        str = new String(new char[1000]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("length", 1000, nStr.length());
        str = new String(new char[10000]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("length overflow", maxLength, nStr.length());

        maxLength = 7;
        str = new String(new char[10]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("max len 7", "aa...aa", nStr);
        maxLength = 6;
        str = new String(new char[10]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("max len 6", "aa...a", nStr);
    }

    @Test
    public void testFormatClassNameToDotDelimiter() {
        String s = StringUtils.formatClassNameToDotDelimiter("com/foo/bar");
        Assert.assertEquals("com.foo.bar", s);
    }

    @Test
    public void testFormatClassNameToSlashDelimiter() {
        String s = StringUtils.formatClassNameToSlashDelimiter("com.foo.bar");
        Assert.assertEquals("com/foo/bar", s);
    }

    @Test
    public void isEmpty() {
        Assert.assertFalse(StringUtils.isEmpty("a"));
        Assert.assertFalse(StringUtils.isEmpty(" ."));
        Assert.assertFalse(StringUtils.isEmpty(". "));

        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty(" "));
        Assert.assertFalse(StringUtils.isEmpty("  "));
    }

    @Test
    public void isBlank() {
        Assert.assertFalse(StringUtils.isBlank("a"));
        Assert.assertFalse(StringUtils.isBlank(" ."));
        Assert.assertFalse(StringUtils.isBlank(". "));

        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank(" "));
        Assert.assertTrue(StringUtils.isBlank("  "));
    }

    @Test
    public void isNotBlank() {
        Assert.assertTrue(StringUtils.isNotBlank("a"));
        Assert.assertTrue(StringUtils.isNotBlank(" ."));
        Assert.assertTrue(StringUtils.isNotBlank(". "));

        Assert.assertFalse(StringUtils.isNotBlank(""));
        Assert.assertFalse(StringUtils.isNotBlank(" "));
        Assert.assertFalse(StringUtils.isNotBlank("  "));
    }

    @Test
    public void replaceChar() {
        String s = StringUtils.replaceChar("gggggggggggggggggg", 'g', 'j');
        Assert.assertEquals("jjjjjjjjjjjjjjjjjj", s);
    }

}