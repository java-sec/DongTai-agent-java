package io.dongtai.iast.common.utils;

import java.util.LinkedList;

/**
 * 固定大小链表
 *
 *
 * TODO 这个工具类好像没有用到，后面考虑移除掉
 *
 * @author liyuan40
 * @date 2022/3/9 20:16
 */
@Deprecated
public class FixSizeLinkedList<T> extends LinkedList<T> {

    private static final long serialVersionUID = 6147000002339841725L;
    private final int capacity;

    public FixSizeLinkedList(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public boolean add(T t) {
        if (size() >= capacity) {
            super.removeFirst();
        }
        return super.add(t);
    }

}
