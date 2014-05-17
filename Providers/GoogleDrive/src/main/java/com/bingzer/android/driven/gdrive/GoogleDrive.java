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
package com.bingzer.android.driven.gdrive;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.DrivenUser;
import com.bingzer.android.driven.api.ResultImpl;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Result;
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

import javax.inject.Inject;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class GoogleDrive implements Driven {

    private static final String defaultFields      = "id,mimeType,title,downloadUrl";
    private static final String defaultFieldsItems = "items(" + defaultFields + ")";
    private static final String TAG = "GoogleDrive";

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Inject GoogleDriveApi.Factory googleDriveApiFactory;
    private static GoogleDriveApi googleDriveApi;
    private static DrivenUser drivenUser;
    private SharedWithMe sharedWithMe;

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isAuthenticated(){
        return googleDriveApi != null && drivenUser != null;
    }

    @Override
    public boolean hasSavedCredentials(Context context) {
        DrivenCredential credential = new DrivenCredential(context);
        return credential.hasSavedCredential(TAG);
    }

    @Override
    public DrivenUser getDrivenUser() throws DrivenException {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return drivenUser;
    }

    public GoogleDriveApi getGoogleDriveApi() throws DrivenException{
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return googleDriveApi;
    }

    public GoogleDriveApi.Factory getGoogleDriveApiFactory(){
        // if it's not injected.. create the default one
        if(googleDriveApiFactory == null) {
            googleDriveApiFactory = new GoogleDriveApi.Factory.Default();
        }

        return googleDriveApiFactory;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Result<DrivenException> authenticate(DrivenCredential credential)  {
        return authenticate(credential, true);
    }

    @Override
    public Result<DrivenException> authenticate(DrivenCredential credential, boolean saveCredential) {
        Log.i(TAG, "Driven API is authenticating with GoogleDrive Service");
        googleDriveApi = null;
        drivenUser = null;

        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            if(credential.hasSavedCredential(TAG)){
                credential.read(TAG);
            }

            googleDriveApi = getGoogleDriveApiFactory().createApi(credential);
            drivenUser = new GoogleDriveUser(googleDriveApi.about().get().setFields("name,user").execute());

            result.setSuccess(true);
            Log.i(TAG, "Driven API successfully authenticated by DriveUser: " + drivenUser);

            // only save when it's not null
            if(saveCredential && credential.getAccountName() != null)
                credential.save(TAG);
        }
        catch (IOException e){
            Log.i(TAG, "Driven API failed to authenticate");
            Log.e(TAG, "Exception:", e);
            result.setException(new DrivenException(e));
        }

        return result;
    }

    @Override
    public void authenticateAsync(final DrivenCredential credential, Task<Result<DrivenException>> result){
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override public Result<DrivenException> invoke() {
                return authenticate(credential);
            }
        });
    }

    @Override
    public void authenticateAsync(final DrivenCredential credential, final boolean saveCredential, Task<Result<DrivenException>> result) {
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override public Result<DrivenException> invoke() {
                return authenticate(credential, saveCredential);
            }
        });
    }

    @Override
    public Result<DrivenException> clearAuthentication(Context context) {
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>(false);
        googleDriveApi = null;
        drivenUser = null;

        DrivenCredential credential = new DrivenCredential(context);
        credential.clear(TAG);

        return result;
    }

    @Override
    public void clearAuthenticationAsync(final Context context, Task<Result<DrivenException>> result) {
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return clearAuthentication(context);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean exists(String name) {
        try {
            FileList list = list("title = '" + name + "'", "id", false);
            return list.getItems().get(0) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(DrivenFile parent, String name) {
        try {
            FileList list = list("'" + parent.getId() + "' in parents AND title = '" + name + "'", "id", false);
            return list.getItems().get(0) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void existsAsync(final String title, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return exists(title);
            }
        });
    }

    @Override
    public void existsAsync(final DrivenFile parent, final String title, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return exists(parent, title);
            }
        });
    }

    @Override
    public DrivenFile id(String id) {
        try{
            return new GoogleDriveFile(getGoogleDriveApi().files().get(id).setFields(defaultFields).execute(), false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void idAsync(final String id, final Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return id(id);
            }
        });
    }

    @Override
    public DrivenFile get(String name) {
        return first("title = '" + name + "'");
    }

    @Override
    public void getAsync(final String title, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return get(title);
            }
        });
    }

    @Override
    public DrivenFile get(DrivenFile parent, String name) {
        return first("'" + parent.getId() + "' in parents AND title = '" + name + "'");
    }

    @Override
    public void getAsync(final DrivenFile parent, final String title, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return get(parent, title);
            }
        });
    }

    @Override
    public DrivenFile update(DrivenFile drivenFile, DrivenContent content) {
        try{
            GoogleDriveFile driveFile = (GoogleDriveFile) drivenFile;
            com.google.api.services.drive.model.File file =
                    getGoogleDriveApi()
                            .files()
                            .update(driveFile.getId(), driveFile.getModel(), new FileContent(content.getType(), content.getFile()))
                            .execute();
            return new GoogleDriveFile(file, drivenFile.hasDetails());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void updateAsync(final DrivenFile drivenFile, final DrivenContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return update(drivenFile, content);
            }
        });
    }

    @Override
    public boolean delete(String id) {
        try {
            getGoogleDriveApi().files().delete(id).execute();
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
            if(fileList != null)
                return new GoogleDriveFile(fileList.getItems().get(0), false);
            return null;
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
            return GoogleDriveFile.from(fileList);
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
    public DrivenFile create(String name, DrivenContent content) {
        return create(null, name, content);
    }

    @Override
    public DrivenFile create(DrivenFile parent, String name) {
        return create(parent, name, null);
    }

    @Override
    public DrivenFile create(DrivenFile parent, String name, DrivenContent content) {
        try{
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(name);

            if (content == null)
                file.setMimeType(GoogleDriveFile.MIME_TYPE_FOLDER);
            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            if(content != null)
                file = getGoogleDriveApi().files().insert(file, new FileContent(content.getType(), content.getFile())).execute();
            else
                file = getGoogleDriveApi().files().insert(file).execute();

            return id(file.getId());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void createAsync(final DrivenFile parent, final String name, final DrivenContent content, Task<DrivenFile> result) {
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
    public void createAsync(final String name, final DrivenContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override public DrivenFile invoke() {
                return create(name, content);
            }
        });
    }

    @Override
    public Iterable<DrivenFile> list() {
        try {
            FileList fileList = list("'root' in parents", defaultFieldsItems, false);
            return GoogleDriveFile.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public Iterable<DrivenFile> list(DrivenFile parent) {
        if(parent == null) return list();

        try {
            FileList fileList = list("'" + parent.getId() + "' in parents", defaultFieldsItems, false);
            return GoogleDriveFile.from(fileList);
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
            return new GoogleDriveFile(getGoogleDriveApi().files().get(drivenFile.getId()).execute(), true);
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
    public DrivenContent download(DrivenFile drivenFile, File local) {
        try{
            GenericUrl url = new GenericUrl(drivenFile.getDownloadUrl());
            HttpRequestFactory factory = getGoogleDriveApi().getRequestFactory();
            HttpRequest request = factory.buildGetRequest(url);
            HttpResponse response = request.execute();

            IOUtils.copyFile(response.getContent(), local);

            return new DrivenContent(drivenFile.getType(), local);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void downloadAsync(final DrivenFile drivenFile, final File local, Task<DrivenContent> result) {
        doAsync(result, new Delegate<DrivenContent>() {
            @Override public DrivenContent invoke() {
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

            getGoogleDriveApi().permissions().insert(drivenFile.getId(), newPermission).execute();
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

    @Override
    public SharedWithMe getShared(){
        if(sharedWithMe == null)
            sharedWithMe = new SharedWithMeImpl();
        return sharedWithMe;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private FileList list(String query, String fields, boolean includeTrashed) throws IOException{
        Drive.Files.List list = getGoogleDriveApi().files().list();

        if(fields != null) list.setFields(fields);
        if(query != null) list.setQ(query);
        if(!includeTrashed) list.setQ(query + (query != null ? " AND" : "") + " trashed = false");

        return list.execute();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private class SharedWithMeImpl implements SharedWithMe {

        @Override
        public DrivenFile get(DrivenFile parent, String name) {
            return first("'" + parent.getId() + "' in parents AND title = '" + name + "' AND sharedWithMe");
        }

        @Override
        public DrivenFile get(String name) {
            return first("title = '" + name + "' AND sharedWithMe");
        }

        @Override
        public void getAsync(final DrivenFile parent, final String title, Task<DrivenFile> result) {
            doAsync(result, new Delegate<DrivenFile>() {
                @Override
                public DrivenFile invoke() {
                    return get(parent, title);
                }
            });
        }

        @Override
        public void getAsync(final String title, Task<DrivenFile> result) {
            doAsync(result, new Delegate<DrivenFile>() {
                @Override
                public DrivenFile invoke() {
                    return get(title);
                }
            });
        }
    }

}
