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
package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Search;
import com.bingzer.android.driven.contracts.Task;

import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * Default Search that implements all "AsyncMethods"
 */
public abstract class AbsSearch implements Search {

    /**
     * Async call for {@link #first(String)}
     */
    @Override
    public void firstAsync(final String query, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return first(query);
            }
        });
    }

    /**
     * Async call for {@link #query(String)}
     */
    @Override
    public void queryAsync(final String query, Task<List<RemoteFile>> task) {
        doAsync(task, new Delegate<List<RemoteFile>>() {
            @Override
            public List<RemoteFile> invoke() {
                return query(query);
            }
        });
    }
}
