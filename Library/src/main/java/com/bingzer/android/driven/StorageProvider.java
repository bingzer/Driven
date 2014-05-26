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

import android.content.Context;

import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.contracts.Trashed;

import java.util.List;

/**
 * Driven is an effort to unify API calls for different cloud storage
 * (i.e: GoogleDrive, Dropbox, OneDrive, etc..). They have different
 * file system and the concept of file is different.
 * But, there are lots of similarities.
 * <p/>
 * At first Driven was used for GoogleDrive API calls but as requirements
 * grow other providers will be implemented as well.
 * <p/>
 * <p/>
 * {@code Driven} will try to address this by unifying all the
 * common calls using this interface.
 */
public interface StorageProvider {

    /**
     * Returns the driven user.
     */
    UserInfo getDrivenUser();

    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Check to see if authenticated
     */
    boolean isAuthenticated();

    /**
     * Check to see if we
     */
    boolean hasSavedCredentials(Context context);

    /**
     * Try to authenticate with specified {@code credential}.
     * Credential (if successful) will be saved automatically
     * for future use
     */
    Result<DrivenException> authenticate(Credential credential);

    /**
     * Try to authenticate with specified {@code credential}.
     * if {@code saveCredential} is {@code true} then the {@code credential}
     * will be saved for future use
     */
    Result<DrivenException> authenticate(Credential credential, boolean saveCredential);

    /**
     * Async call for {@link #authenticate(Credential)}
     */
    void authenticateAsync(Credential credential, Task<Result<DrivenException>> task);

    /**
     * Async call for {@link #authenticate(Credential, boolean)}
     */
    void authenticateAsync(Credential credential, boolean saveCredential, Task<Result<DrivenException>> task);

    /**
     * Clear authentication. This will credential cache if any
     */
    Result<DrivenException> clearAuthentication(Context context);

    /**
     * Async call for {@link #clearAuthentication(android.content.Context)}
     */
    void clearAuthenticationAsync(Context context, Task<Result<DrivenException>> task);

    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Check to see if a file exists
     */
    boolean exists(String name);

    /**
     * Check to see if a file exists in a specified parent.
     * if {@code parent} is null, then it should check the root directory
     * for the file
     */
    boolean exists(RemoteFile parent, String name);

    /**
     * Async API for {@link #exists(String)}
     */
    void existsAsync(String title, Task<Boolean> task);

    /**
     * Async call for {@link #exists(RemoteFile, String)}
     */
    void existsAsync(RemoteFile parent, String title, Task<Boolean> task);

    /**
     * Returns {@link RemoteFile} if found by its name
     * in a specified {@code parent}
     */
    RemoteFile get(RemoteFile parent, String name);

    /**
     * Returns {@link RemoteFile} (if found) by its name
     */
    RemoteFile get(String name);

    /**
     * Async call for {@link #get(RemoteFile, String)}
     */
    void getAsync(RemoteFile parent, String title, Task<RemoteFile> task);

    /**
     * Async call fro {@link #get(String)}
     */
    void getAsync(String title, Task<RemoteFile> task);

    /**
     * Returns {@link RemoteFile} by its Id (if found)
     */
    RemoteFile id(String id);

    /**
     * Async call for {@link #id(String)}
     */
    void idAsync(String id, Task<RemoteFile> task);

    /**
     * Returns the details for {@link RemoteFile}
     */
    RemoteFile getDetails(RemoteFile remoteFile);

    /**
     * Async call for {@link #getDetails(RemoteFile)}
     */
    void getDetailsAsync(RemoteFile remoteFile, Task<RemoteFile> task);

    /**
     * Returns a collection of {@link RemoteFile}s found
     * in the root directory
     */
    List<RemoteFile> list();

    /**
     * Returns a collection of {@link RemoteFile}s found
     * in a specified directory ({@code parent})
     */
    List<RemoteFile> list(RemoteFile parent);

    /**
     * Async call for {@link #list(RemoteFile)}
     */
    void listAsync(RemoteFile folder, Task<List<RemoteFile>> task);

    /**
     * Async call for {@link #list()}
     */
    void listAsync(Task<List<RemoteFile>> task);

    /**
     * Creates a directory with its name in the root directory
     */
    RemoteFile create(String name);

    /**
     * Creates a file with its name in the root directory
     */
    RemoteFile create(String name, LocalFile content);

    /**
     * Creates a directory (with its name) in the {@code parent} directory
     */
    RemoteFile create(RemoteFile parent, String name);

    /**
     * Creates a file in a directory (with its name) in the {@code parent} directory
     */
    RemoteFile create(RemoteFile parent, String name, LocalFile content);

    /**
     * Async call for {@link #create(RemoteFile, String, LocalFile)}
     */
    void createAsync(RemoteFile parent, String name, LocalFile content, Task<RemoteFile> task);

    /**
     * Async call for {@link #create(RemoteFile, String)}
     */
    void createAsync(RemoteFile parent, String name, Task<RemoteFile> task);

    /**
     * Async call for {@link #create(String, LocalFile)}
     */
    void createAsync(String name, LocalFile content, Task<RemoteFile> task);

    /**
     * Async call for {@link #create(String)}
     */
    void createAsync(String name, Task<RemoteFile> task);

    /**
     * Update a file. Content of file is specified by {@link LocalFile}
     */
    RemoteFile update(RemoteFile remoteFile, LocalFile content);

    /**
     * Async call for {@link #update(RemoteFile, LocalFile)}
     */
    void updateAsync(RemoteFile remoteFile, LocalFile content, Task<RemoteFile> task);

    /**
     * Deletes a file specified by its Id.
     * Most of the time you probably want to get the {@link RemoteFile}
     * first then called {@link RemoteFile#delete()}.
     * <p/>
     * Note that {@code id} is required to be a valid id
     */
    boolean delete(String id);

    /**
     * Async call for {@link #delete(String)}
     */
    void deleteAsync(String id, Task<Boolean> task);

    /**
     * Returns the first {@link RemoteFile} found in the query.
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

    /**
     * Download remote file to {@code local}. Most of the time
     * you would use {@link RemoteFile#download(LocalFile)}
     * rather than calling this method
     */
    boolean download(RemoteFile remoteFile, LocalFile local);

    /**
     * Async all for {@link #download(RemoteFile, LocalFile)}
     */
    void downloadAsync(RemoteFile remoteFile, LocalFile local, Task<Boolean> task);

    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the "SharedWithMe" interface
     */
    SharedWithMe getShared();

    /**
     * Returns the "Sharing" interface
     */
    Sharing getSharing();

    /**
     * Returns the "Trashed" interface
     */
    Trashed getTrashed();

}
