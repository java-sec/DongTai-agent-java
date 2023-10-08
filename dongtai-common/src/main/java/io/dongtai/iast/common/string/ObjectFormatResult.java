package io.dongtai.iast.common.string;

/**
 * 用于表示对 对象格式化的结果
 *
 * @author CC11001100
 * @since 1.13.2
 */
public class ObjectFormatResult {

    // 对象格式化后的字符串，可能不是原始的完整的字符串是被格式化过的，仅作为展示之类的使用
    // 比如原始字符串可能是一个超长的字符串：
    // "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"，
    // 因为字符串太长了，则可能会发生省略，格式化之后的字符串就变成了这样：
    // "aaaaaaaaaaaaaaaaaa...aaaaaaaaaaaaaa"
    public String objectFormatString;

    // 原始的字符串长度，对象格式化可以认为有三个步骤：
    //
    // object --> original string --> format string
    //
    // 其中original string通常是调用object的toString()得到的，长度可能会比较短，比如："aaa"，也可能老长老长了，比如：
    // "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    // 注意这个长度可能会导致非常巨大的资源占用，而这个对象格式化器如此大费周章就是为了避免掉对象的字符串完整表示形式在内存中同时展开浪费的资源
    // format string 这一步是对 original string 进行截断，控制字符串的长度，这个格式化过的字符串才是真正上报的字符串，而这个字符串的长度是可控的
    public int originalLength;

}
