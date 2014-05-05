package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.Task;

/**
 * Created by Ricky on 5/3/2014.
 */
class TestUsage {

    void test(){
        final Driven driven = Driven.getDriven();


        driven.getAsync("", new Task<DriveFile>() {
            @Override
            public void onCompleted(DriveFile result) {
            }
        });

        driven.getAsync("", new Task.WithErrorReporting<DriveFile>() {
            @Override
            public void onError(Throwable error) {

            }

            @Override
            public void onCompleted(DriveFile result) {

            }
        });

        driven.query("title = 'BabyCare'");
        driven.first("title = 'babyCare'");
    }
}
