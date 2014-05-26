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

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.UserInfo;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.contracts.Trashed;
import com.bingzer.android.driven.utils.IOUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class GoogleDrive implements StorageProvider {

    private static final String defaultFields      = "id,mimeType,title,downloadUrl";
    private static final String defaultFieldsItems = "items(" + defaultFields + ")";
    private static final String TAG = "GoogleDrive";

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Inject GoogleDriveApi.Factory googleDriveApiFactory;
    private static GoogleDriveApi googleDriveApi;
    private static UserInfo userInfo;
    private SharedWithMe sharedWithMe;
    private Sharing sharing;
    private Trashed trashed;

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isAuthenticated(){
        return googleDriveApi != null && userInfo != null;
    }

    @Override
    public boolean hasSavedCredentials(Context context) {
        Credential credential = new Credential(context);
        return credential.hasSavedCredential(TAG);
    }

    @Override
    public UserInfo getDrivenUser() throws DrivenException {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return userInfo;
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
    public Result<DrivenException> authenticate(Credential credential)  {
        return authenticate(credential, true);
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential, boolean saveCredential) {
        Log.i(TAG, "Driven API is authenticating with GoogleDrive Service");
        googleDriveApi = null;
        userInfo = null;

        Result<DrivenException> result = new Result<DrivenException>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            if(credential.hasSavedCredential(TAG)){
                credential.read(TAG);
            }

            googleDriveApi = getGoogleDriveApiFactory().createApi(credential);
            userInfo = new GoogleDriveUserInfo(googleDriveApi.about().get().setFields("name,user").execute());

            result.setSuccess(true);
            Log.i(TAG, "Driven API successfully authenticated by DriveUser: " + userInfo);

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
    public void authenticateAsync(final Credential credential, Task<Result<DrivenException>> task){
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override public Result<DrivenException> invoke() {
                return authenticate(credential);
            }
        });
    }

    @Override
    public void authenticateAsync(final Credential credential, final boolean saveCredential, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override public Result<DrivenException> invoke() {
                return authenticate(credential, saveCredential);
            }
        });
    }

    @Override
    public Result<DrivenException> clearAuthentication(Context context) {
        Result<DrivenException> result = new Result<DrivenException>(false);
        googleDriveApi = null;
        userInfo = null;

        Credential credential = new Credential(context);
        credential.clear(TAG);

        return result;
    }

    @Override
    public void clearAuthenticationAsync(final Context context, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
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
            return exists("title = '" + name + "'", "id", false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(RemoteFile parent, String name) {
        try {
            return exists("'" + parent.getId() + "' in parents AND title = '" + name + "'", "id", false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void existsAsync(final String title, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return exists(title);
            }
        });
    }

    @Override
    public void existsAsync(final RemoteFile parent, final String title, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return exists(parent, title);
            }
        });
    }

    @Override
    public RemoteFile id(String id) {
        try{
            return new GoogleDriveFile(getGoogleDriveApi().files().get(id).setFields(defaultFields).execute(), false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void idAsync(final String id, final Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return id(id);
            }
        });
    }

    @Override
    public RemoteFile get(String name) {
        return first("title = '" + name + "'");
    }

    @Override
    public void getAsync(final String title, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return get(title);
            }
        });
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        return first("'" + parent.getId() + "' in parents AND title = '" + name + "'");
    }

    @Override
    public void getAsync(final RemoteFile parent, final String title, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return get(parent, title);
            }
        });
    }

    @Override
    public RemoteFile update(RemoteFile remoteFile, LocalFile content) {
        try{
            GoogleDriveFile driveFile = (GoogleDriveFile) remoteFile;
            com.google.api.services.drive.model.File file;
            if(content == null){
                file = getGoogleDriveApi()
                        .files()
                        .update(driveFile.getId(), driveFile.getModel())
                        .execute();
            }
            else {
                file = getGoogleDriveApi()
                        .files()
                        .update(driveFile.getId(), driveFile.getModel(), new com.google.api.client.http.FileContent(content.getType(), content.getFile()))
                        .execute();
            }
            return new GoogleDriveFile(file, remoteFile.hasDetails());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void updateAsync(final RemoteFile remoteFile, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return update(remoteFile, content);
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
    public void deleteAsync(final String id, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return delete(id);
            }
        });
    }

    @Override
    public RemoteFile first(String query) {
        try{
            return first(query, defaultFieldsItems, true);
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
    public void firstAsync(final String query, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return first(query);
            }
        });
    }

    @Override
    public java.util.List<RemoteFile> query(String query) {
        try{
            return list(query, defaultFieldsItems, true);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void queryAsync(final String query, Task<java.util.List<RemoteFile>> task) {
        doAsync(task, new Delegate<java.util.List<RemoteFile>>() {
            @Override public java.util.List<RemoteFile> invoke() {
                return query(query);
            }
        });
    }

    @Override
    public RemoteFile create(String name) {
        return create(name, null);
    }

    @Override
    public RemoteFile create(String name, LocalFile content) {
        return create(null, name, content);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        return create(parent, name, null);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name, LocalFile content) {
        try{
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(name);

            if (content == null)
                file.setMimeType(GoogleDriveFile.MIME_TYPE_FOLDER);
            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            if(content != null)
                file = getGoogleDriveApi().files().insert(file, new com.google.api.client.http.FileContent(content.getType(), content.getFile())).execute();
            else
                file = getGoogleDriveApi().files().insert(file).execute();

            return id(file.getId());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void createAsync(final RemoteFile parent, final String name, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>(){
            @Override public RemoteFile invoke() {
                return create(parent, name, content);
            }
        });
    }

    @Override
    public void createAsync(final RemoteFile parent, final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return create(parent, name);
            }
        });
    }

    @Override
    public void createAsync(final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return create(name);
            }
        });
    }

    @Override
    public void createAsync(final String name, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return create(name, content);
            }
        });
    }

    @Override
    public java.util.List<RemoteFile> list() {
        try {
            return list("'root' in parents", defaultFieldsItems, false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public java.util.List<RemoteFile> list(RemoteFile parent) {
        if(parent == null) return list();

        try {
            return list("'" + parent.getId() + "' in parents", defaultFieldsItems, false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void listAsync(final RemoteFile folder, Task<java.util.List<RemoteFile>> task) {
        doAsync(task, new Delegate<java.util.List<RemoteFile>>() {
            @Override public java.util.List<RemoteFile> invoke() {
                return list(folder);
            }
        });
    }

    @Override
    public void listAsync(Task<java.util.List<RemoteFile>> task) {
        doAsync(task, new Delegate<java.util.List<RemoteFile>>() {
            @Override public java.util.List<RemoteFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public RemoteFile getDetails(RemoteFile remoteFile) {
        try{
            return new GoogleDriveFile(getGoogleDriveApi().files().get(remoteFile.getId()).execute(), true);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public void getDetailsAsync(final RemoteFile remoteFile, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override public RemoteFile invoke() {
                return getDetails(remoteFile);
            }
        });
    }

    @Override
    public boolean download(RemoteFile remoteFile, LocalFile local) {
        try{
            GenericUrl url = new GenericUrl(remoteFile.getDownloadUrl());
            HttpRequestFactory factory = getGoogleDriveApi().getRequestFactory();
            HttpRequest request = factory.buildGetRequest(url);
            HttpResponse response = request.execute();

            IOUtils.copyFile(response.getContent(), local.getFile());

            return true;
        }
        catch (IOException e){
            return false;
        }
    }

    @Override
    public void downloadAsync(final RemoteFile remoteFile, final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return download(remoteFile, local);
            }
        });
    }

    @Override
    public SharedWithMe getShared(){
        if(sharedWithMe == null)
            sharedWithMe = new SharedWithMeImpl();
        return sharedWithMe;
    }

    @Override
    public Trashed getTrashed(){
        if(trashed == null)
            trashed = new TrashedImpl();
        return trashed;
    }

    public Sharing getSharing(){
        if(sharing == null)
            sharing = new SharingImpl();
        return sharing;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private boolean exists(String query, String fields, boolean includeTrashed) throws IOException {
        return first(query, fields, includeTrashed) != null;
    }


    private RemoteFile first(String query, String fields, boolean includeTrash) throws IOException {
        List<RemoteFile> list = list(query, fields, includeTrash);
        if(list != null && list.size() > 0)
            return list.get(0);
        return null;
    }

    private List<RemoteFile> list(String query, String fields, boolean includeTrashed) throws IOException{
        Drive.Files.List list = getGoogleDriveApi().files().list();

        if(fields != null) list.setFields(fields);
        if(query != null) list.setQ(query);
        if(!includeTrashed) list.setQ(query + (query != null ? " AND" : "") + " trashed = false");

        return list(list.execute());
    }

    private List<RemoteFile> list(FileList fileList){
        if(fileList == null) return null;

        List<RemoteFile> list = new ArrayList<RemoteFile>();
        for(int i = 0; i < fileList.getItems().size(); i++){
            list.add(new GoogleDriveFile(fileList.getItems().get(i), false));
        }

        return list;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    class SharingImpl implements Sharing {

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public String share(RemoteFile remoteFile, String user) {
            return share(remoteFile, user, PERMISSION_DEFAULT);
        }

        @Override
        public String share(RemoteFile remoteFile, String user, int kind) {
            try{
                Permission permission = new Permission();

                permission.setValue(user);
                permission.setType("user");
                permission.setRole(getPermission(kind));

                permission = getGoogleDriveApi().permissions().insert(remoteFile.getId(), permission).execute();
                return permission.getSelfLink();
            }
            catch (IOException e){
                return null;
            }
        }

        @Override
        public void shareAsync(final RemoteFile remoteFile, final String user, Task<String> result) {
            doAsync(result, new Delegate<String>() {
                @Override public String invoke() {
                    return share(remoteFile, user);
                }
            });
        }

        @Override
        public void shareAsync(final RemoteFile remoteFile, final String user, final int kind, Task<String> result) {
            doAsync(result, new Delegate<String>() {
                @Override public String invoke() {
                    return share(remoteFile, user, kind);
                }
            });
        }

        private String getPermission(int kind){
            switch (kind){
                default: return "writer";
                case PERMISSION_READ: return "read";
                case PERMISSION_FULL: return "writer";
            }
        }
    }

    class SharedWithMeImpl implements SharedWithMe {

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public boolean exists(String name) {
            return first("title = '" + name + "' AND sharedWithMe") != null;
        }

        @Override
        public void existsAsync(final String name, Task<Boolean> result) {
            doAsync(result, new Delegate<Boolean>() {
                @Override public Boolean invoke() {
                    return exists(name);
                }
            });
        }

        @Override
        public RemoteFile get(String name) {
            try {
                return GoogleDrive.this.first("title = '" + name + "' AND sharedWithMe", defaultFields, false);
            }
            catch (IOException e) {
                return null;
            }
        }

        @Override
        public void getAsync(final String name, Task<RemoteFile> result) {
            doAsync(result, new Delegate<RemoteFile>() {
                @Override
                public RemoteFile invoke() {
                    return get(name);
                }
            });
        }

        @Override
        public List<RemoteFile> list() {
            try{
                return GoogleDrive.this.list("sharedWithMe", defaultFieldsItems, false);
            }
            catch (IOException e){
                return null;
            }
        }

        @Override
        public void listAsync(Task<List<RemoteFile>> result) {
            doAsync(result, new Delegate<List<RemoteFile>>() {
                @Override public List<RemoteFile> invoke() {
                    return list();
                }
            });
        }
    }

    class TrashedImpl implements Trashed {

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public boolean exists(String name) {
            try {
                return GoogleDrive.this.exists("'title' = " + name + "'", defaultFields, true);
            }
            catch (IOException e){
                return false;
            }
        }

        @Override
        public void existsAsync(final String name, Task<Boolean> result) {
            doAsync(result, new Delegate<Boolean>() {
                @Override public Boolean invoke() {
                    return exists(name);
                }
            });
        }

        @Override
        public RemoteFile get(String name) {
            try {
                return GoogleDrive.this.first("'title' = " + name + "'", "id", true);
            }
            catch (IOException e){
                return null;
            }
        }

        @Override
        public void getAsync(final String name, Task<RemoteFile> result) {
            doAsync(result, new Delegate<RemoteFile>() {
                @Override public RemoteFile invoke() {
                    return get(name);
                }
            });
        }

        @Override
        public List<RemoteFile> list() {
            try {
                return GoogleDrive.this.list("", defaultFieldsItems, true);
            }
            catch (IOException e) {
                return null;
            }
        }

        @Override
        public void listAsync(Task<List<RemoteFile>> result) {
            doAsync(result, new Delegate<List<RemoteFile>>() {
                @Override public List<RemoteFile> invoke() {
                    return list();
                }
            });
        }
    }

}
