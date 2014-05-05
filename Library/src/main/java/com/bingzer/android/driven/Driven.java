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

import android.os.AsyncTask;
import android.util.Log;

import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.DrivenApi;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.IOUtils;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class Driven
        implements DrivenApi.Get, DrivenApi.Get.ByTitle,
                    DrivenApi.Post, DrivenApi.Put,
                    DrivenApi.Delete, DrivenApi.Query,
                    DrivenApi.List, DrivenApi.Details,
                    DrivenApi.Download, DrivenApi.Share{

    private static final String defaultFields = "items(id,mimeType,title,downloadUrl)";
    private static final String TAG = "Driven";

    private Drive service;
    private final SharedWithMe sharedWithMe = new SharedWithMeImpl();

    public static Driven getDriven(){
        return null;
    }

    private Driven(){
        // nothing
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void authenticate(Drive service) throws DrivenException {
        // throw not implemented
        this.service = service;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DriveFile get(String id) {
        try{
            return new DriveFile(service.files().get(id).setFields(defaultFields).execute(), false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void getAsync(final String id, final Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return get(id);
            }
        });
    }

    @Override
    public DriveFile getByTitle(DriveFile parent, String title) {
        return first("'" + parent.getId() + "' in parents AND title = '" + title + "'");
    }

    @Override
    public DriveFile getByTitle(String title) {
        return first("'title = '" + title + "'");
    }

    @Override
    public void getByTitleAsync(final DriveFile parent, final String title, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return getByTitle(parent, title);
            }
        });
    }

    @Override
    public void getByTitleAsync(final String title, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return getByTitle(title);
            }
        });
    }

    @Override
    public DriveFile update(DriveFile driveFile, FileContent content) {
        try{
            return new DriveFile(service.files().update(driveFile.getId(), driveFile.getModel()).execute(), driveFile.hasDetails());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void updateAsync(final DriveFile driveFile, final FileContent content, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return update(driveFile, content);
            }
        });
    }

    @Override
    public boolean delete(String id) {
        try {
            Void v = service.files().delete(id)
                    .execute();
            return v != null;
        }
        catch (IOException e){
            return false;
        }
    }

    @Override
    public void delete(final String id, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return delete(id);
            }
        });
    }

    @Override
    public DriveFile first(String query) {
        try{
            FileList fileList = list(query, defaultFields, true);
            return new DriveFile(fileList.getItems().get(0), false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void firstAsync(final String query, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return first(query);
            }
        });
    }

    @Override
    public Iterable<DriveFile> query(String query) {
        try{
            FileList fileList = list(query, defaultFields, true);
            return DriveFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void query(final String query, Task<Iterable<DriveFile>> result) {
        doAsync(result, new Delegate<Iterable<DriveFile>>() {
            @Override public Iterable<DriveFile> invoke() {
                return query(query);
            }
        });
    }

    @Override
    public DriveFile create(String name) {
        return create(name, null);
    }

    @Override
    public DriveFile create(String name, FileContent content) {
        return create(null, name, content);
    }

    @Override
    public DriveFile create(DriveFile parent, String name) {
        return create(parent, name, null);
    }

    @Override
    public DriveFile create(DriveFile parent, String name, FileContent content) {
        try{
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(name);

            if (content == null)
                file.setMimeType(DriveFile.MIME_TYPE_FOLDER);
            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            if(content != null)
                file = service.files().insert(file, content).execute();
            else
                file = service.files().insert(file).execute();

            return get(file.getId());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void createAsync(final DriveFile parent, final String name, final FileContent content, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>(){
            @Override public DriveFile invoke() {
                return create(parent, name, content);
            }
        });
    }

    @Override
    public void createAsync(final DriveFile parent, final String name, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return create(parent, name);
            }
        });
    }

    @Override
    public void createAsync(final String name, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return create(name);
            }
        });
    }

    @Override
    public void createAsync(final String name, final FileContent content, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return create(name, content);
            }
        });
    }

    @Override
    public Iterable<DriveFile> list(DriveFile folder) {
        try {
            FileList fileList = list("'" + folder.getId() + "' in parents", defaultFields, false);
            return DriveFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void listAsync(final DriveFile folder, Task<Iterable<DriveFile>> result) {
        doAsync(result, new Delegate<Iterable<DriveFile>>() {
            @Override public Iterable<DriveFile> invoke() {
                return list(folder);
            }
        });
    }

    @Override
    public DriveFile getDetails(DriveFile driveFile) {
        try{
            return new DriveFile(service.files().get(driveFile.getId()).execute(), true);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void getDetailsAsync(final DriveFile driveFile, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return getDetails(driveFile);
            }
        });
    }

    @Override
    public File download(DriveFile driveFile, File local) {
        try{
            GenericUrl url = new GenericUrl(driveFile.getModel().getDownloadUrl());
            HttpRequestFactory factory = service.getRequestFactory();
            HttpRequest request = factory.buildGetRequest(url);
            HttpResponse response = request.execute();

            IOUtils.copyFile(response.getContent(), local);
            return local;
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void downloadAsync(final DriveFile driveFile, final File local, Task<File> result) {
        doAsync(result, new Delegate<File>() {
            @Override public File invoke() {
                return download(driveFile, local);
            }
        });
    }

    @Override
    public boolean share(DriveFile driveFile, String user) {
        try{
            Permission newPermission = new Permission();

            newPermission.setValue(user);
            newPermission.setType("user");
            newPermission.setRole("writer");

            service.permissions().insert(driveFile.getId(), newPermission).execute();
            return true;
        }
        catch (IOException e){
            return false;
        }
    }

    @Override
    public void shareAsync(final DriveFile driveFile, final String user, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return share(driveFile, user);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public SharedWithMe getShared(){
        return sharedWithMe;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private FileList list(String query, String fields, boolean includeTrashed) throws IOException{
        return service.files().list()
                .setFields(fields)
                .setQ(query + (includeTrashed ? "" : " AND trashed = false"))
                .execute();
    }

    @SuppressWarnings("unchecked")
    protected <T> void doAsync(final Task<T> task, final Delegate<T> action){
        new AsyncTask<Void, Void, T>(){

            @Override protected T doInBackground(Void... params) {
                try {
                    return action.invoke();
                }
                catch (Throwable e){
                    reportError(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(T result) {
                task.onCompleted(result);
            }

            void reportError(Throwable error){
                if(task instanceof Task.WithErrorReporting) {
                    ((Task.WithErrorReporting) task).onError(error);
                }
                else
                    Log.e(TAG, "Error occurred in AsyncTask", error);
            }
        }.execute();
    }

}