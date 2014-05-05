package com.bingzer.android.driven.contracts;

/**
 * Created by Ricky on 5/5/2014.
 */
public interface Progress<T> {

    int getProgress();

    String getMessage();

    T getResult();
}
