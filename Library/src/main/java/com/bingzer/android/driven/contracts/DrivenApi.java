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

import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.DrivenException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;

import java.io.File;

/**
 * Created by Ricky on 5/3/2014.
 */
@SuppressWarnings("unused")
public interface DrivenApi {

    public static interface Exists {

        boolean exists(String title);

        boolean exists(DrivenFile parent, String title);

        void existsAsync(String title, Task<Boolean> result);

        void existsAsync(DrivenFile parent, String title, Task<Boolean> result);

    }

    public static interface Auth {

        Result<DrivenException> authenticate(GoogleAccountCredential credential);

        Result<DrivenException> authenticate(GoogleAccountCredential credential, boolean saveCredential);

        void authenticateAsync(GoogleAccountCredential credential, Task<Result<DrivenException>> result);

        void authenticateAsync(GoogleAccountCredential credential, boolean saveCredential, Task<Result<DrivenException>> result);

        Result<DrivenException> deauthenticate(Context context);

        void deauthenticateAsync(Context context, Task<Result<DrivenException>> result);
    }

    public static interface Get {

        DrivenFile get(DrivenFile parent, String title);

        DrivenFile get(String title);

        void getAsync(DrivenFile parent, String title, Task<DrivenFile> result);

        void getAsync(String title, Task<DrivenFile> result);

        public static interface ById {

            DrivenFile id(String id);

            void idAsync(String id, Task<DrivenFile> result);
        }

    }

    public static interface Details {

        DrivenFile getDetails(DrivenFile drivenFile);

        void getDetailsAsync(DrivenFile drivenFile, Task<DrivenFile> result);

    }

    public static interface List {

        Iterable<DrivenFile> list();

        Iterable<DrivenFile> list(DrivenFile folder);

        void listAsync(DrivenFile folder, Task<Iterable<DrivenFile>> result);

        void listAsync(Task<Iterable<DrivenFile>> result);

    }

    public static interface Post {

        DrivenFile create(String name);

        DrivenFile create(String name, FileContent content);

        DrivenFile create(DrivenFile parent, String name);

        DrivenFile create(DrivenFile parent, String name, FileContent content);

        void createAsync(DrivenFile parent, String name, FileContent content, Task<DrivenFile> result);

        void createAsync(DrivenFile parent, String name, Task<DrivenFile> result);

        void createAsync(String name, Task<DrivenFile> result);

        void createAsync(String name, FileContent content, Task<DrivenFile> result);

    }

    public static interface Put {

        DrivenFile update(DrivenFile drivenFile, FileContent content);

        void updateAsync(DrivenFile drivenFile, FileContent content, Task<DrivenFile> result);
    }

    public static interface Delete {

        boolean delete(String id);

        void deleteAsync(String id, Task<Boolean> result);

    }

    public static interface Query {

        DrivenFile first(String query);

        void firstAsync(String query, Task<DrivenFile> result);

        Iterable<DrivenFile> query(String query);

        void queryAsync(String query, Task<Iterable<DrivenFile>> result);

    }

    public static interface Download {

        File download(DrivenFile drivenFile, File local);

        void downloadAsync(DrivenFile drivenFile, File local, Task<File> result);

    }

    public static interface Share {

        boolean share(DrivenFile drivenFile, String user);

        void shareAsync(DrivenFile drivenFile, String user, Task<Boolean> result);

    }

}
