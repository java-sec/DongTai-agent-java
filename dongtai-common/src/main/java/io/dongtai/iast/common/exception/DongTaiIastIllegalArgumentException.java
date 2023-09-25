package io.dongtai.iast.common.exception;

/**
 * 洞态内部的参数错误异常
 *
 * @author CC11001100
 */
public class DongTaiIastIllegalArgumentException extends DongTaiIastException {

    public DongTaiIastIllegalArgumentException() {
    }

    public DongTaiIastIllegalArgumentException(String s) {
        super(s);
    }

    public DongTaiIastIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DongTaiIastIllegalArgumentException(Throwable cause) {
        super(cause);
    }

}
