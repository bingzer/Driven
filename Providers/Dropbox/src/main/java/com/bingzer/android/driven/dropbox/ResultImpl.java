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
package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.contracts.Result;

/**
 * Created by Ricky on 5/5/2014.
 */
class ResultImpl<T extends Throwable> implements Result<T>{
    private boolean success;
    private T exception;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setException(T exception) {
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getException() {
        return exception;
    }
}
