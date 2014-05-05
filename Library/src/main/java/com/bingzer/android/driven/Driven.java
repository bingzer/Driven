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

import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.IDrivenApi;
import com.bingzer.android.driven.utils.DriveFileUtils;
import com.bingzer.android.driven.utils.IOUtils;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.File;
import java.io.IOException;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class Driven
        implements IDrivenApi.Get, IDrivenApi.Post, IDrivenApi.Put,
                    IDrivenApi.Delete, IDrivenApi.Query,
                    IDrivenApi.List, IDrivenApi.Details,
                    IDrivenApi.Download, IDrivenApi.Share{

    private static final String defaultFields = "items(id,mimeType,title,downloadUrl)";
    private static final String TAG = "Driven";

    private Drive service;

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
        return first("id = '" + id + "'", defaultFields, true);
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
    public DriveFile update(DriveFile driveFile, FileContent content) {
        try{
            return new DriveFile(service.files().update(driveFile.getId(), driveFile.getModel()).execute());
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
    public Iterable<DriveFile> query(String query) {
        try{
            FileList fileList = list(query, defaultFields, true);
            return DriveFileUtils.toIterables(fileList);
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
    public DriveFile create(DriveFile parent, String name, FileContent content) {
        return null;
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
    public Iterable<DriveFile> list(DriveFile folder) {
        try {
            FileList fileList = list("'" + folder.getId() + "' in parents", defaultFields, false);
            return DriveFileUtils.toIterables(fileList);
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
        return first("id = '" + driveFile.getId() + "'", null, true);
    }

    @Override
    public Iterable<DriveFile> getDetails(Iterable<DriveFile> driveFiles) {
        try {
            StringBuilder query = new StringBuilder("id ");

            for (DriveFile driveFile : driveFiles) {
                query.append("= '").append(driveFile.getId()).append("' ");
                query.append("AND");
            }

            FileList fileList = list(query.toString(), null, true);
            return DriveFileUtils.toIterables(fileList);
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
    public void getDetailsAsync(final Iterable<DriveFile> driveFiles, Task<Iterable<DriveFile>> result) {
        doAsync(result, new Delegate<Iterable<DriveFile>>() {
            @Override public Iterable<DriveFile> invoke() {
                return getDetails(driveFiles);
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

    protected DriveFile first(String query, String fields, boolean includeTrashed){
        try {
            FileList list = list(query, fields, includeTrashed);

            if (list != null && list.getItems().size() > 0) {
                return new DriveFile(list.getItems().get(0));
            }
        }
        catch (IOException e){
            Log.e(TAG, "on Driven.first(" + query + ")", e);
        }

        return null;
    }

    protected FileList list(String query, String fields, boolean includeTrashed) throws IOException{
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
