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

import com.bingzer.android.driven.contracts.Task;

import java.util.List;

/**
 * Represents a remote file.
 * Every provider implements and treat remote file differently.
 * Consult the provider's documentation.
 */
@SuppressWarnings("unused")
public interface RemoteFile {

    /**
     * The unique identifier.
     */
    public String getId();

    /**
     * True if this file is a directory
     */
    public boolean isDirectory();

    /**
     * The name of this file
     */
    public String getName();

    /**
     * The MIME type of this file
     */
    public String getType();

    /**
     * The public downloadable URL.
     * Driven API should be able to use this URL
     * to download this file on demand.
     * It may or may not use authenticate scheme
     * by the provider.
     */
    public String getDownloadUrl();

    /**
     * True if this file has "all" the details (metadata)
     * provided by the provider.
     */
    public boolean hasDetails();

    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fetch this file's complete metadata
     */
    public boolean fetchDetails();

    /**
     * Async for {@link #fetchDetails()}
     */
    public void fetchDetailsAsync(Task<Boolean> result);

    /**
     * If this file is {@link #isDirectory()} then this
     * method should return all children within.
     */
    public List<RemoteFile> list();

    /**
     * Async for {@link #list()}
     */
    public void listAsync(Task<List<RemoteFile>> result);

    /**
     * Download and keep this file to the local.
     * There should be no local-to-remote mapping.
     */
    public boolean download(LocalFile local);

    /**
     * Async for {@link #download(LocalFile)}
     */
    public void downloadAsync(LocalFile local, Task<Boolean> result);

    /**
     * Upload/Save a file to this file. The remote content file
     * will be overwritten by the {@code content}
     */
    public boolean upload(LocalFile local);

    /**
     * Async for {@link #upload(com.bingzer.android.driven.LocalFile)}
     */
    public void uploadAsync(LocalFile local, Task<Boolean> result);

    /**
     * Share this file to other user. "Sharing" is generic and you should
     * refer to the Provider's documentation
     */
    public String share(String user);

    /**
     * Share this file to other user. "Sharing" is generic and you should
     * refer to the Provider's documentation
     */
    public String share(String user, int kind);

    /**
     * Async for {@link #share(String)}
     */
    public void shareAsync(String user, Task<String> result);

    /**
     * Async for {@link #share(String, int)}
     */
    public void shareAsync(String user, int kind, Task<String> result);

    /**
     * Delete or removes this file from remote provider.
     */
    public boolean delete();

    /**
     * Async for {@link #delete()}
     */
    public void deleteAsync(Task<Boolean> result);

    /**
     * Rename the name of this file. True if successful,
     * false otherwise
     */
    public boolean rename(String name);

    /**
     * Async call for {@link #rename(String)}
     */
    public void renameAsync(String name, Task<Boolean> result);

}
