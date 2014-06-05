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
 * Contract for search
 */
public interface Search extends Feature {

    /**
     * Returns the first {@link com.bingzer.android.driven.RemoteFile} found in the query.
     * Query is provider-dependent. Check with provider's documentation and implementation.
     */
    RemoteFile first(String query);

    /**
     * Async call for {@link #first(String)}
     */
    void firstAsync(String query, Task<RemoteFile> task);

    /**
     * Search for a {@code query} and returns all {@link RemoteFile}
     * that matches the criteria of the specified {@code query}.
     * Query is provider-dependent. Check with provider's documentation and implementation.
     */
    List<RemoteFile> query(String query);

    /**
     * Async call for {@link #query(String)}
     */
    void queryAsync(String query, Task<List<RemoteFile>> task);

}
