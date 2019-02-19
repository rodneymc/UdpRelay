package com.daftdroid.android.udprelay;

public class RelayException extends Exception {
    static final long serialVersionUID = 1L;
    public RelayException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public RelayException(String msg) {
        super(msg);
    }
}
