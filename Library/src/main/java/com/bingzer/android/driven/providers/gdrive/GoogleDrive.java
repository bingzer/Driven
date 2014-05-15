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
package com.bingzer.android.driven.providers.gdrive;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenProvider;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.DrivenUser;
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
public final class GoogleDrive implements DrivenProvider {

    private static final String defaultFields      = "id,mimeType,title,downloadUrl";
    private static final String defaultFieldsItems = "items(" + defaultFields + ")";
    private static final String TAG = "Driven";

    /////////////////////////////////////////////////////////////////////////////////////////////

    private Proxy proxy;
    private DrivenUser drivenUser;
    private final SharedWithMe sharedWithMe;
    @Inject
    ProxyCreator proxyCreator;

    public GoogleDrive(){
        sharedWithMe = new SharedWithMeImpl(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isAuthenticated(){
        return proxy != null && drivenUser != null;
    }

    @Override
    public DrivenUser getDrivenUser() throws DrivenException {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return drivenUser;
    }

    public Proxy getProxy() throws DrivenException{
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return proxy;
    }

    public ProxyCreator getProxyCreator(){
        // if it's not injected.. create the default one
        if(proxyCreator == null) {
            proxyCreator = new ProxyCreator.Default();
        }

        return proxyCreator;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Result<DrivenException> authenticate(DrivenCredential credential)  {
        return authenticate(credential, true);
    }

    @Override
    public Result<DrivenException> authenticate(DrivenCredential credential, boolean saveCredential) {
        Log.i(TAG, "Driven API is authenticating with GoogleDrive Service");
        proxy = null;
        drivenUser = null;

        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            String accountName = readSavedCredentials(credential.getContext());
            if (credential.getAccountName() == null && accountName != null) {
                credential.setAccountName(accountName);
            }

            proxy = getProxyCreator().createProxy(credential);
            drivenUser = new DrivenUserImpl(proxy.about().get().setFields("name,user").execute());

            result.setSuccess(true);
            Log.i(TAG, "Driven API successfully authenticated by DriveUser: " + drivenUser);

            // only save when it's not null
            if(saveCredential && credential.getAccountName() != null)
                saveCredentials(credential);
        }
        catch (IOException e){
            Log.i(TAG, "Driven API cannot authenticate using account name: " + credential.getAccountName());
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
    public Result<DrivenException> deauthenticate(Context context) {
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        proxy = null;
        drivenUser = null;
        result.setSuccess(getCredentialFile(context).delete());
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
    public boolean exists(String title) {
        try {
            FileList list = list("title = '" + title + "'", "id", false);
            return list.getItems().get(0) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(DrivenFile parent, String title) {
        try {
            FileList list = list("'" + parent.getId() + "' in parents AND title = '" + title + "'", "id", false);
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
            return new DrivenFileImpl(getProxy().files().get(id).setFields(defaultFields).execute(), false);
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
    public DrivenFile get(String title) {
        return first("title = '" + title + "'");
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
    public DrivenFile get(DrivenFile parent, String title) {
        return first("'" + parent.getId() + "' in parents AND title = '" + title + "'");
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
            DrivenFileImpl driveFile = (DrivenFileImpl) drivenFile;
            com.google.api.services.drive.model.File file =
                    getProxy()
                            .files()
                            .update(driveFile.getId(), driveFile.getModel(), new FileContent(content.getType(), content.getFile()))
                            .execute();
            return new DrivenFileImpl(file, drivenFile.hasDetails());
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
            getProxy().files().delete(id).execute();
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
                return new DrivenFileImpl(fileList.getItems().get(0), false);
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
            return DrivenFileImpl.from(fileList);
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
                file.setMimeType(DrivenFileImpl.MIME_TYPE_FOLDER);
            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            if(content != null)
                file = getProxy().files().insert(file, new FileContent(content.getType(), content.getFile())).execute();
            else
                file = getProxy().files().insert(file).execute();

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
            FileList fileList = list(null, defaultFieldsItems, false);
            return DrivenFileImpl.from(fileList);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public Iterable<DrivenFile> list(DrivenFile folder) {
        try {
            FileList fileList = list("'" + folder.getId() + "' in parents", defaultFieldsItems, false);
            return DrivenFileImpl.from(fileList);
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
            return new DrivenFileImpl(getProxy().files().get(drivenFile.getId()).execute(), true);
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
            HttpRequestFactory factory = getProxy().getRequestFactory();
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

            getProxy().permissions().insert(drivenFile.getId(), newPermission).execute();
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
        Drive.Files.List list = getProxy().files().list();

        if(fields != null) list.setFields(fields);
        if(query != null) list.setQ(query);
        if(includeTrashed) list.setQ(query + (query != null ? " AND" : "") + " trashed = false");

        return list.execute();
    }

    private File getCredentialFile(Context context){
        File dir = context.getFilesDir();
        return new File(dir, "credential");
    }

    private void saveCredentials(DrivenCredential credential) throws IOException{
        FileWriter writer = null;
        try{
            writer = new FileWriter(getCredentialFile(credential.getContext()));
            writer.write(credential.getAccountName());
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
