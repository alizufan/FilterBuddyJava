package com.alizufan.filterbuddy.exception;

public class FailedFetchFilterRules extends Exception {

    public FailedFetchFilterRules(String msg, Throwable th) {
        super(msg, th);
    }

    public FailedFetchFilterRules(String msg) {
        super(msg);
    }

    public FailedFetchFilterRules() {
        super();
    }

}
