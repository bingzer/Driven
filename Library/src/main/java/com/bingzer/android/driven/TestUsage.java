package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.Task;

/**
 * Created by Ricky on 5/3/2014.
 */
class TestUsage {

    void test(){
        final Driven driven = Driven.getDriven();


        driven.getAsync("", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
            }
        });

        driven.getAsync("", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onError(Throwable error) {

            }

            @Override
            public void onCompleted(DrivenFile result) {

            }
        });

        driven.query("title = 'BabyCare'");
        driven.first("title = 'babyCare'");
        DrivenFile drivenFile = driven.title("BabyCare");
    }
}
