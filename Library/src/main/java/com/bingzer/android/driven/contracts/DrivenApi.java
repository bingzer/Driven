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

import android.content.Context;

import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.DrivenException;

import java.io.File;

/**
 * API calls
 */
@SuppressWarnings("unused")
public interface DrivenApi {

    public static interface Exists {

        /**
         * Check to see if a file exists
         */
        boolean exists(String name);

        /**
         * Check to see if a file exists in a specified parent.
         * if {@code parent} is null, then it should check the root directory
         * for the file
         */
        boolean exists(DrivenFile parent, String name);

        /**
         * Async API for {@link #exists(String)}
         */
        void existsAsync(String title, Task<Boolean> result);

        /**
         * Async call for {@link #exists(com.bingzer.android.driven.DrivenFile, String)}
         */
        void existsAsync(DrivenFile parent, String title, Task<Boolean> result);

    }

    public static interface Auth {

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
        Result<DrivenException> authenticate(DrivenCredential credential);

        /**
         * Try to authenticate with specified {@code credential}.
         * if {@code saveCredential} is {@code true} then the {@code credential}
         * will be saved for future use
         */
        Result<DrivenException> authenticate(DrivenCredential credential, boolean saveCredential);

        /**
         * Async call for {@link #authenticate(com.bingzer.android.driven.DrivenCredential)}
         */
        void authenticateAsync(DrivenCredential credential, Task<Result<DrivenException>> result);

        /**
         * Async call for {@link #authenticate(com.bingzer.android.driven.DrivenCredential, boolean)}
         */
        void authenticateAsync(DrivenCredential credential, boolean saveCredential, Task<Result<DrivenException>> result);

        /**
         * Clear authentication. This will credential cache if any
         */
        Result<DrivenException> clearAuthentication(Context context);

        /**
         * Async call for {@link #clearAuthentication(android.content.Context)}
         */
        void clearAuthenticationAsync(Context context, Task<Result<DrivenException>> result);
    }

    public static interface Get {

        /**
         * Returns {@link com.bingzer.android.driven.DrivenFile} if found by its name
         * in a specified {@code parent}
         */
        DrivenFile get(DrivenFile parent, String name);

        /**
         * Returns {@link com.bingzer.android.driven.DrivenFile} (if found) by its name
         */
        DrivenFile get(String name);

        /**
         * Async call for {@link #get(com.bingzer.android.driven.DrivenFile, String)}
         */
        void getAsync(DrivenFile parent, String title, Task<DrivenFile> result);

        /**
         * Async call fro {@link #get(String)}
         */
        void getAsync(String title, Task<DrivenFile> result);

        public static interface ById {

            /**
             * Returns {@link com.bingzer.android.driven.DrivenFile} by its Id (if found)
             */
            DrivenFile id(String id);

            /**
             * Async call for {@link #id(String)}
             */
            void idAsync(String id, Task<DrivenFile> result);
        }

    }

    public static interface Details {

        /**
         * Returns the details for {@link com.bingzer.android.driven.DrivenFile}
         */
        DrivenFile getDetails(DrivenFile drivenFile);

        /**
         * Async call for {@link #getDetails(com.bingzer.android.driven.DrivenFile)}
         */
        void getDetailsAsync(DrivenFile drivenFile, Task<DrivenFile> result);

    }

    public static interface List {

        /**
         * Returns a collection of {@link com.bingzer.android.driven.DrivenFile}s found
         * in the root directory
         */
        Iterable<DrivenFile> list();

        /**
         * Returns a collection of {@link com.bingzer.android.driven.DrivenFile}s found
         * in a specified directory ({@code parent})
         */
        Iterable<DrivenFile> list(DrivenFile parent);

        /**
         * Async call for {@link #list(com.bingzer.android.driven.DrivenFile)}
         */
        void listAsync(DrivenFile folder, Task<Iterable<DrivenFile>> result);

        /**
         * Async call for {@link #list()}
         */
        void listAsync(Task<Iterable<DrivenFile>> result);

    }

    public static interface Post {

        /**
         * Creates a directory with its name in the root directory
         */
        DrivenFile create(String name);

        /**
         * Creates a file with its name in the root directory
         */
        DrivenFile create(String name, DrivenContent content);

        /**
         * Creates a directory (with its name) in the {@code parent} directory
         */
        DrivenFile create(DrivenFile parent, String name);

        /**
         * Creates a file in a directory (with its name) in the {@code parent} directory
         */
        DrivenFile create(DrivenFile parent, String name, DrivenContent content);

        /**
         * Async call for {@link #create(com.bingzer.android.driven.DrivenFile, String, com.bingzer.android.driven.DrivenContent)}
         */
        void createAsync(DrivenFile parent, String name, DrivenContent content, Task<DrivenFile> result);

        /**
         * Async call for {@link #create(com.bingzer.android.driven.DrivenFile, String)}
         */
        void createAsync(DrivenFile parent, String name, Task<DrivenFile> result);

        /**
         * Async call for {@link #create(String, com.bingzer.android.driven.DrivenContent)}
         */
        void createAsync(String name, DrivenContent content, Task<DrivenFile> result);

        /**
         * Async call for {@link #create(String)}
         */
        void createAsync(String name, Task<DrivenFile> result);

    }

    public static interface Put {

        /**
         * Update a file. Content of file is specified by {@link com.bingzer.android.driven.DrivenContent}
         */
        DrivenFile update(DrivenFile drivenFile, DrivenContent content);

        /**
         * Async call for {@link #update(com.bingzer.android.driven.DrivenFile, com.bingzer.android.driven.DrivenContent)}
         */
        void updateAsync(DrivenFile drivenFile, DrivenContent content, Task<DrivenFile> result);
    }

    public static interface Delete {

        /**
         * Deletes a file specified by its Id.
         * Most of the time you probably want to get the {@link com.bingzer.android.driven.DrivenFile}
         * first then called {@link com.bingzer.android.driven.DrivenFile#delete()}.
         * <p/>
         * Note that {@code id} is required to be a valid id
         */
        boolean delete(String id);

        /**
         * Async call for {@link #delete(String)}
         */
        void deleteAsync(String id, Task<Boolean> result);

    }

    public static interface Query {

        /**
         * Returns the first {@link com.bingzer.android.driven.DrivenFile} found in the query.
         * Query is provider-dependent. Check with provider's documentation and implementation.
         */
        DrivenFile first(String query);

        /**
         * Async call for {@link #first(String)}
         */
        void firstAsync(String query, Task<DrivenFile> result);

        /**
         * Search for a {@code query} and returns all {@link com.bingzer.android.driven.DrivenFile}
         * that matches the criteria of the specified {@code query}.
         * Query is provider-dependent. Check with provider's documentation and implementation.
         */
        Iterable<DrivenFile> query(String query);

        /**
         * Async call for {@link #query(String)}
         */
        void queryAsync(String query, Task<Iterable<DrivenFile>> result);

    }

    public static interface Download {

        /**
         * Download remote file to {@code local}. Most of the time
         * you would use {@link com.bingzer.android.driven.DrivenFile#download(java.io.File)}
         * rather than calling this method
         */
        File download(DrivenFile drivenFile, File local);

        /**
         * Async all for {@link #download(com.bingzer.android.driven.DrivenFile, java.io.File)}
         */
        void downloadAsync(DrivenFile drivenFile, File local, Task<File> result);

    }

    public static interface Share {

        /**
         * Share {@link com.bingzer.android.driven.DrivenFile} to other user.
         * The term "user" is generic. Most of the time it is an email address.
         * Check with provider's implementation.
         */
        boolean share(DrivenFile drivenFile, String user);

        /**
         * Async call for {@link #share(com.bingzer.android.driven.DrivenFile, String)}
         */
        void shareAsync(DrivenFile drivenFile, String user, Task<Boolean> result);

    }

    /*
    TODO: We should differentiate "Share" and "Collaborate".
    public static interface Collaborate {

        boolean collaborate(DrivenFile drivenFile, String user);
    }
    */

}
