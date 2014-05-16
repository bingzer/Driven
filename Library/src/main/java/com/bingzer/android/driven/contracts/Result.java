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
 * Result from a Delegate after invoke() is called
 *
 */
public interface Result<T extends Throwable> {

    /**
     * True if successful
     */
    public boolean isSuccess();

    /**
     * If successfull, this should be null.
     * Otherwise that this is the exception that
     * is generated explaining why the result is a
     * failure
     */
    public T getException();

}
