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
    String getId();

    /**
     * True if this file is a directory
     */
    boolean isDirectory();

    /**
     * The name of this file
     */
    String getName();

    /**
     * The MIME type of this file
     */
    String getType();

    /**
     * The public downloadable URL.
     * Driven API should be able to use this URL
     * to download this file on demand.
     * It may or may not use authenticate scheme
     * by the provider.
     */
    String getDownloadUrl();

    /**
     * True if this file has "all" the details (metadata)
     * provided by the provider.
     */
    boolean hasDetails();

    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fetch this file's complete metadata
     */
    boolean fetchDetails();

    /**
     * Async for {@link #fetchDetails()}
     */
    void fetchDetailsAsync(Task<Boolean> task);

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * If this file is {@link #isDirectory()} then
     * create a directory under this directory
     */
    RemoteFile create(String name);

    /**
     * If this file is {@link #isDirectory()} then
     * creates a file with under this directory
     */
    RemoteFile create(String name, LocalFile content);

    /**
     * Async call for {@link #create(String, LocalFile)}
     */
    void createAsync(String name, LocalFile content, Task<RemoteFile> task);

    /**
     * Async call for {@link #create(String)}
     */
    void createAsync(String name, Task<RemoteFile> task);

    /**
     * If this file is {@link #isDirectory()} then this
     * method should return a single children the specified {@code name}
     */
    RemoteFile get(String name);

    /**
     * Async for {@link #get(String)}
     */
    void getAsync(String name, Task<RemoteFile> task);

    /**
     * If this file is {@link #isDirectory()} then this
     * method should return all children within.
     */
    List<RemoteFile> list();

    /**
     * Async for {@link #list()}
     */
    void listAsync(Task<List<RemoteFile>> task);

    /**
     * Download and keep this file to the local.
     * There should be no local-to-remote mapping.
     */
    boolean download(LocalFile local);

    /**
     * Async for {@link #download(LocalFile)}
     */
    void downloadAsync(LocalFile local, Task<Boolean> task);

    /**
     * Upload/Save a file to this file. The remote content file
     * will be overwritten by the {@code content}
     */
    boolean upload(LocalFile local);

    /**
     * Async for {@link #upload(com.bingzer.android.driven.LocalFile)}
     */
    void uploadAsync(LocalFile local, Task<Boolean> task);

    /**
     * Share this file to other user. "Sharing" is generic and you should
     * refer to the Provider's documentation
     */
    String share(String user);

    /**
     * Share this file to other user. "Sharing" is generic and you should
     * refer to the Provider's documentation
     */
    String share(String user, int kind);

    /**
     * Async for {@link #share(String)}
     */
    void shareAsync(String user, Task<String> task);

    /**
     * Async for {@link #share(String, int)}
     */
    void shareAsync(String user, int kind, Task<String> task);

    /**
     * Delete or removes this file from remote provider.
     */
    boolean delete();

    /**
     * Async for {@link #delete()}
     */
    void deleteAsync(Task<Boolean> task);

    /**
     * Rename the name of this file. True if successful,
     * false otherwise
     */
    boolean rename(String name);

    /**
     * Async call for {@link #rename(String)}
     */
    void renameAsync(String name, Task<Boolean> task);

}
