package com.bingzer.android.driven;

/**
 * Created by Ricky on 5/4/2014.
 */
final class Async<T> {
    protected T object;

    Async(T object, Runnable runnable){
        this.object = object;
    }

    public T getObject(){
        return object;
    }
}
