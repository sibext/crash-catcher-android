package com.sibext.android.tools;

/**
 * @author mbelsky 30.04.14
 */
public class CrashCatcherError extends Error {

    public CrashCatcherError(String detailMessage) {
        super(detailMessage);
    }

    public CrashCatcherError(Throwable throwable) {
        super(throwable);
    }
}
