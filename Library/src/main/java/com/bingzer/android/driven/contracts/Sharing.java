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

/**
 * Contracts for sharing
 */
public interface Sharing extends Feature {

    /**
     * True if Provider's implementation support "Sharing"
     */
    boolean isSupported();

    /**
     * Share {@link com.bingzer.android.driven.RemoteFile} to other user.
     * The term "user" is generic. Most of the time it is an email address.
     * Check with provider's implementation.
     */
    String share(RemoteFile remoteFile, String user);

    /**
     * Share {@link com.bingzer.android.driven.RemoteFile} to other user.
     * The term "user" is generic. Most of the time it is an email address.
     * Check with provider's implementation.
     *
     * @see com.bingzer.android.driven.Permission#PERMISSION_OWNER
     * @see com.bingzer.android.driven.Permission#PERMISSION_READ
     * @see com.bingzer.android.driven.Permission#PERMISSION_FULL
     */
    String share(RemoteFile remoteFile, String user, int kind);

    /**
     * Async call for {@link #share(com.bingzer.android.driven.RemoteFile, String)}
     */
    void shareAsync(RemoteFile remoteFile, String user, Task<String> task);

    /**
     * Async call for {@link #share(com.bingzer.android.driven.RemoteFile, String, int)}
     */
    void shareAsync(RemoteFile remoteFile, String user, int kind, Task<String> task);

    /**
     * Remove specific user from their share access
     */
    boolean removeSharing(RemoteFile remoteFile, String user);

    /**
     * Async call for {@link #removeSharing(com.bingzer.android.driven.RemoteFile, String)} }}
     */
    void removeSharingAsync(RemoteFile remoteFile, String user, Task<Boolean> task);
}
