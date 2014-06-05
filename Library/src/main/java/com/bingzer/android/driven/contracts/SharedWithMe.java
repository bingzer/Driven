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

import com.bingzer.android.driven.RemoteFile;

import java.util.List;

/**
 * "SharedWithMe" contracts.
 */
public interface SharedWithMe extends Feature {

    /**
     * True if Provider's implementation support "SharedWithMe"
     */
    boolean isSupported();

    /**
     * Check to see if there's a file by its name in the SharedWithMe directory
     */
    boolean exists(String name);

    /**
     * Async call for {@link #exists(String)}
     */
    void existsAsync(String name, Task<Boolean> task);

    /**
     * Returns {@link com.bingzer.android.driven.RemoteFile} (if found) by its name
     */
    RemoteFile get(String name);

    /**
     * Async call fro {@link #get(String)}
     */
    void getAsync(String name, Task<RemoteFile> task);

    /**
     * Returns a collection of {@link com.bingzer.android.driven.RemoteFile}s found
     * in the root directory
     */
    List<RemoteFile> list();

    /**
     * Async call for {@link #list()}
     */
    void listAsync(Task<List<RemoteFile>> task);

}
