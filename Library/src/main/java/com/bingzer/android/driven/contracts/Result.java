package com.bingzer.android.driven.contracts;

/**
 * Created by Ricky on 5/5/2014.
 */
public interface Result<T extends Throwable> {
    public boolean isSuccess();

    public T getException();
}
