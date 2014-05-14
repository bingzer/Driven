/**
 * Copyright 2014 Ricky Tobing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance insert the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bingzer.android.driven.utils;

import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ricky on 5/6/2014.
 */
public final class AsyncUtils {

    private static ThreadPoolExecutor threadPoolExecutor;

    static {
        final int numCore = Runtime.getRuntime().availableProcessors();
        final LinkedBlockingQueue<Runnable> workerQueue = new LinkedBlockingQueue<Runnable>();
        threadPoolExecutor = new ThreadPoolExecutor(numCore, numCore, 1, TimeUnit.SECONDS, workerQueue);
    }

    public static <T> void doAsync(final Task<T> task, final Delegate<T> action){
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    T result = action.invoke();
                    task.onCompleted(result);
                }
                catch (Throwable error){
                    if(task instanceof Task.WithErrorReporting){
                        ((Task.WithErrorReporting) task).onError(error);
                    }
                    else throw new DrivenException(error);
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private AsyncUtils(){
        // nothing
    }
}
