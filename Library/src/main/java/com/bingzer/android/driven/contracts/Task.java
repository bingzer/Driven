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
package com.bingzer.android.driven.contracts;

/**
 * Created by Ricky on 5/3/2014.
 */
public interface Task<T> {

    /**
     * Called when a task is completed
     * @param result result
     */
    void onCompleted(T result);

    /**
     * Extension if you need some kind of explanation
     * what a task failed to complete
     * @param <T>
     */
    public static interface WithErrorReporting<T> extends Task<T> {

        /**
         * Called when there's an error during the invocation
         */
        void onError(Throwable error);

    }
}
