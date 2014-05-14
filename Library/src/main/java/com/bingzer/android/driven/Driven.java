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
import com.bingzer.android.driven.contracts.DrivenService;
import com.bingzer.android.driven.contracts.DrivenServiceProvider;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Task;
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

import javax.inject.Inject;

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

    private DrivenService drivenService;
    private DrivenUser drivenUser;
    private final SharedWithMe sharedWithMe;
    @Inject DrivenServiceProvider serviceProvider;


    Driven(){
        sharedWithMe = new SharedWithMeImpl(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isAuthenticated(){
        return drivenService != null && drivenUser != null;
    }

    public DrivenUser getDrivenUser() throws DrivenException{
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return drivenUser;
    }

    public DrivenService getDrivenService() throws DrivenException{
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return drivenService;
    }

    public DrivenServiceProvider getServiceProvider(){
        // if it's not injected.. create the default one
        if(serviceProvider == null) {
            serviceProvider = new GoogleDriveServiceProvider();
        }

        return serviceProvider;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public Result<DrivenException> authenticate(GoogleAccountCredential credential)  {
        return authenticate(credential, true);
    }

    @Override
    public Result<DrivenException> authenticate(GoogleAccountCredential credential, boolean saveCredential) {
        Log.i(TAG, "Driven API is authenticating with GoogleDrive Service");
        drivenService = null;
        drivenUser = null;

        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            String accountName = readSavedCredentials(credential.getContext());
            if (credential.getSelectedAccountName() == null && accountName != null) {
                credential.setSelectedAccountName(accountName);
            }

            drivenService = getServiceProvider().createGoogleDriveService(credential);
            drivenUser = new DrivenUser(drivenService.about().get().setFields("name,user").execute());

            result.setSuccess(true);
            Log.i(TAG, "Driven API successfully authenticated by DriveUser: " + drivenUser);

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

    @Override
    public Result<DrivenException> deauthenticate(Context context) {
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        drivenService = null;
        drivenUser = null;
        return result;
    }

    @Override
    public void deauthenticateAsync(final Context context, Task<Result<DrivenException>> result) {
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return deauthenticate(context);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DrivenFile get(String id) {
        try{
            return new DrivenFile(getDrivenService().files().get(id).setFields(defaultFields).execute(), false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void getAsync(final String id, final Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return get(id);
            }
        });
    }

    @Override
    public DrivenFile title(String title) {
        return first("title = '" + title + "'");
    }

    @Override
    public void titleAsync(final String title, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return title(title);
            }
        });
    }

    @Override
    public DrivenFile title(DrivenFile parent, String title) {
        return first("'" + parent.getId() + "' in parents AND title = '" + title + "'");
    }

    @Override
    public void titleAsync(final DrivenFile parent, final String title, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return title(parent, title);
            }
        });
    }

    @Override
    public DrivenFile update(DrivenFile drivenFile, FileContent content) {
        try{
            com.google.api.services.drive.model.File file =
                    getDrivenService()
                        .files()
                        .update(drivenFile.getId(), drivenFile.getModel(), content)
                        .execute();
            return new DrivenFile(file, drivenFile.hasDetails());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void updateAsync(final DrivenFile drivenFile, final FileContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return update(drivenFile, content);
            }
        });
    }

    @Override
    public boolean delete(String id) {
        try {
            getDrivenService().files().delete(id).execute();
            return true;
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
    public DrivenFile first(String query) {
        try{
            FileList fileList = list(query, defaultFieldsItems, true);
            return new DrivenFile(fileList.getItems().get(0), false);
        }
        catch (IOException e){
            return null;
        }
        catch (IndexOutOfBoundsException e){
            // not found
            return null;
        }
    }

    @Override
    public void firstAsync(final String query, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return first(query);
            }
        });
    }

    @Override
    public Iterable<DrivenFile> query(String query) {
        try{
            FileList fileList = list(query, defaultFieldsItems, true);
            return DrivenFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void queryAsync(final String query, Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override public Iterable<DrivenFile> invoke() {
                return query(query);
            }
        });
    }

    @Override
    public DrivenFile create(String name) {
        return create(name, null);
    }

    @Override
    public DrivenFile create(String name, FileContent content) {
        return create(null, name, content);
    }

    @Override
    public DrivenFile create(DrivenFile parent, String name) {
        return create(parent, name, null);
    }

    @Override
    public DrivenFile create(DrivenFile parent, String name, FileContent content) {
        try{
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(name);

            if (content == null)
                file.setMimeType(DrivenFile.MIME_TYPE_FOLDER);
            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            if(content != null)
                file = getDrivenService().files().insert(file, content).execute();
            else
                file = getDrivenService().files().insert(file).execute();

            return get(file.getId());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void createAsync(final DrivenFile parent, final String name, final FileContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>(){
            @Override public DrivenFile invoke() {
                return create(parent, name, content);
            }
        });
    }

    @Override
    public void createAsync(final DrivenFile parent, final String name, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return create(parent, name);
            }
        });
    }

    @Override
    public void createAsync(final String name, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return create(name);
            }
        });
    }

    @Override
    public void createAsync(final String name, final FileContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return create(name, content);
            }
        });
    }

    @Override
    public Iterable<DrivenFile> list() {
        try {
            FileList fileList = list(null, defaultFieldsItems, false);
            return DrivenFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public Iterable<DrivenFile> list(DrivenFile folder) {
        try {
            FileList fileList = list("'" + folder.getId() + "' in parents", defaultFieldsItems, false);
            return DrivenFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void listAsync(final DrivenFile folder, Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override public Iterable<DrivenFile> invoke() {
                return list(folder);
            }
        });
    }

    @Override
    public void listAsync(Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override public Iterable<DrivenFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public DrivenFile getDetails(DrivenFile drivenFile) {
        try{
            return new DrivenFile(getDrivenService().files().get(drivenFile.getId()).execute(), true);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void getDetailsAsync(final DrivenFile drivenFile, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return getDetails(drivenFile);
            }
        });
    }

    @Override
    public File download(DrivenFile drivenFile, File local) {
        try{
            GenericUrl url = new GenericUrl(drivenFile.getDownloadUrl());
            HttpRequestFactory factory = getDrivenService().getRequestFactory();
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
    public void downloadAsync(final DrivenFile drivenFile, final File local, Task<File> result) {
        doAsync(result, new Delegate<File>() {
            @Override public File invoke() {
                return download(drivenFile, local);
            }
        });
    }

    @Override
    public boolean share(DrivenFile drivenFile, String user) {
        try{
            Permission newPermission = new Permission();

            newPermission.setValue(user);
            newPermission.setType("user");
            newPermission.setRole("writer");

            getDrivenService().permissions().insert(drivenFile.getId(), newPermission).execute();
            return true;
        }
        catch (IOException e){
            return false;
        }
    }

    @Override
    public void shareAsync(final DrivenFile drivenFile, final String user, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return share(drivenFile, user);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public SharedWithMe getShared(){
        return sharedWithMe;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private FileList list(String query, String fields, boolean includeTrashed) throws IOException{
        Drive.Files.List list = getDrivenService().files().list();

        if(fields != null) list.setFields(fields);
        if(query != null) list.setQ(query);
        if(includeTrashed) list.setQ(query + (query != null ? " AND" : "") + " trashed = false");

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
