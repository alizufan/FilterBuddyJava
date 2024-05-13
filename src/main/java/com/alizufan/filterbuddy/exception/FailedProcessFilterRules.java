package com.alizufan.filterbuddy.exception;

public class FailedProcessFilterRules extends Exception {

    public FailedProcessFilterRules(String msg, Throwable th) {
        super(msg, th);
    }

    public FailedProcessFilterRules(String msg) {
        super(msg);
    }

    public FailedProcessFilterRules() {
        super();
    }

}
