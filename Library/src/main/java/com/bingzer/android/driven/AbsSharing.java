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
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Task;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
* Created by Ricky on 6/4/2014.
*/
public abstract class AbsSharing implements Sharing {

    /**
     * Async call for {@link #share(RemoteFile, String)}
     */
    @Override
    public void shareAsync(final RemoteFile remoteFile, final String user, Task<String> task) {
        doAsync(task, new Delegate<String>() {
            @Override
            public String invoke() {
                return share(remoteFile, user);
            }
        });
    }

    /**
     * Async call for {@link #share(RemoteFile, String, int)}
     */
    @Override
    public void shareAsync(final RemoteFile remoteFile, final String user, final int kind, Task<String> task) {
        doAsync(task, new Delegate<String>() {
            @Override
            public String invoke() {
                return share(remoteFile, user, kind);
            }
        });
    }

    @Override
    public void removeSharingAsync(final RemoteFile remoteFile, final String user, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return removeSharing(remoteFile, user);
            }
        });
    }

}
