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
import android.util.Log;

import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.DrivenApi;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.DriveUtils;
import com.bingzer.android.driven.utils.IOUtils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class Driven implements DrivenApi.Auth,
                    DrivenApi.Get, DrivenApi.Get.ByTitle,
                    DrivenApi.Post, DrivenApi.Put,
                    DrivenApi.Delete, DrivenApi.Query,
                    DrivenApi.List, DrivenApi.Details,
                    DrivenApi.Download, DrivenApi.Share{

    private static final Driven driven = new Driven();
    private static final String defaultFields      = "id,mimeType,title,downloadUrl";
    private static final String defaultFieldsItems = "items(" + defaultFields + ")";
    private static final String TAG = "Driven";

    public static Driven getDriven(){
        return driven;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private Drive driveService;
    private DriveUser driveUser;
    private final SharedWithMe sharedWithMe;

    private Driven(){
        sharedWithMe = new SharedWithMeImpl(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isAuthenticated(){
        return driveService != null && driveUser != null;
    }

    public DriveUser getDriveUser() throws DrivenException{
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return driveUser;
    }

    public Drive getDriveService() throws DrivenException{
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return driveService;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public Result<DrivenException> authenticate(GoogleAccountCredential credential)  {
        return authenticate(credential, true);
    }

    @Override
    public Result<DrivenException> authenticate(GoogleAccountCredential credential, boolean saveCredential) {
        Log.i(TAG, "Driven API is authenticating with GoogleDrive Service");

        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            String accountName = readSavedCredentials(credential.getContext());
            if (credential.getSelectedAccountName() == null && accountName != null) {
                credential.setSelectedAccountName(accountName);
            }

            driveService = DriveUtils.createGoogleDriveService(credential);
            driveUser = new DriveUser(driveService.about().get().setFields("name,user").execute());

            result.setSuccess(true);
            Log.i(TAG, "Driven API successfully authenticated by DriveUser: " + driveUser);

            // only save when it's not null
            if(saveCredential && credential.getSelectedAccountName() != null)
                saveCredentials(credential);
        }
        catch (IOException e){
            Log.i(TAG, "Driven API cannot authenticate using account name: " + credential.getSelectedAccountName());
            result.setException(new DrivenException(e));
        }

        return result;
    }

    public void authenticateAsync(final GoogleAccountCredential credential, Task<Result<DrivenException>> result){
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override public Result<DrivenException> invoke() {
                return authenticate(credential);
            }
        });
    }

    @Override
    public void authenticateAsync(final GoogleAccountCredential credential, final boolean saveCredential, Task<Result<DrivenException>> result) {
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override public Result<DrivenException> invoke() {
                return authenticate(credential, saveCredential);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DriveFile get(String id) {
        try{
            return new DriveFile(getDriveService().files().get(id).setFields(defaultFields).execute(), false);
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
    public DriveFile title(String title) {
        return first("title = '" + title + "'");
    }

    @Override
    public void titleAsync(final String title, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return title(title);
            }
        });
    }

    @Override
    public DriveFile title(DriveFile parent, String title) {
        return first("'" + parent.getId() + "' in parents AND title = '" + title + "'");
    }

    @Override
    public void titleAsync(final DriveFile parent, final String title, Task<DriveFile> result) {
        doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return title(parent, title);
            }
        });
    }

    @Override
    public DriveFile update(DriveFile driveFile, FileContent content) {
        try{
            return new DriveFile(getDriveService().files().update(driveFile.getId(), driveFile.getModel()).execute(), driveFile.hasDetails());
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
            Void v = getDriveService().files().delete(id)
                    .execute();
            return v != null;
        }
        catch (IOException e){
            return false;
        }
    }

    @Override
    public void deleteAsync(final String id, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return delete(id);
            }
        });
    }

    @Override
    public DriveFile first(String query) {
        try{
            FileList fileList = list(query, defaultFieldsItems, true);
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
            FileList fileList = list(query, defaultFieldsItems, true);
            return DriveFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void queryAsync(final String query, Task<Iterable<DriveFile>> result) {
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
                file = getDriveService().files().insert(file, content).execute();
            else
                file = getDriveService().files().insert(file).execute();

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
    public Iterable<DriveFile> list() {
        try {
            FileList fileList = list(null, defaultFieldsItems, false);
            return DriveFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public Iterable<DriveFile> list(DriveFile folder) {
        try {
            FileList fileList = list("'" + folder.getId() + "' in parents", defaultFieldsItems, false);
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
    public void listAsync(Task<Iterable<DriveFile>> result) {
        doAsync(result, new Delegate<Iterable<DriveFile>>() {
            @Override public Iterable<DriveFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public DriveFile getDetails(DriveFile driveFile) {
        try{
            return new DriveFile(getDriveService().files().get(driveFile.getId()).execute(), true);
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
            HttpRequestFactory factory = getDriveService().getRequestFactory();
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

            getDriveService().permissions().insert(driveFile.getId(), newPermission).execute();
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
        Drive.Files.List list = getDriveService().files().list();

        String includeTrashQuery = (includeTrashed ? null : " AND trashed = false");

        if(query != null) list.setQ(query + includeTrashQuery);
        else if(includeTrashQuery != null) list.setQ(includeTrashQuery);

        if(fields != null) list.setFields(fields);

        return list.execute();
    }

    private File getCredentialFile(Context context){
        File dir = context.getFilesDir();
        return new File(dir, "credential");
    }

    private void saveCredentials(GoogleAccountCredential credential) throws IOException{
        FileWriter writer = null;
        try{
            writer = new FileWriter(getCredentialFile(credential.getContext()));
            writer.write(credential.getSelectedAccountName());
            writer.flush();
            writer.close();
        }
        catch (IOException e){
            Log.e(TAG, "Failed to save credentials to file", e);
        }
        finally {
            if(writer != null) writer.close();
        }
    }

    private String readSavedCredentials(Context context) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(getCredentialFile(context)));
            return reader.readLine().trim();
        }
        catch (IOException e){
            return null;
        }
        finally {
            if(reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    Log.wtf(TAG, "Failed when attempting to close");
                }
            }
        }
    }

}
