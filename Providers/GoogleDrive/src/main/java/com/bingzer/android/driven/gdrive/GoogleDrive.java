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

import com.bingzer.android.driven.AbsPermission;
import com.bingzer.android.driven.AbsSearch;
import com.bingzer.android.driven.AbsSharedWithMe;
import com.bingzer.android.driven.AbsSharing;
import com.bingzer.android.driven.AbsStorageProvider;
import com.bingzer.android.driven.AbsTrashed;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.DefaultUserInfo;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.UserInfo;
import com.bingzer.android.driven.UserRole;
import com.bingzer.android.driven.contracts.Search;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Trashed;
import com.bingzer.android.driven.utils.IOUtils;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class GoogleDrive extends AbsStorageProvider {

    private static final String defaultFields      = "id,mimeType,title,downloadUrl";
    private static final String defaultFieldsItems = "items(" + defaultFields + ")";

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Inject GoogleDriveApi.Factory googleDriveApiFactory;
    private static GoogleDriveApi googleDriveApi;
    private static UserInfo userInfo;

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isAuthenticated(){
        return googleDriveApi != null && userInfo != null;
    }

    @Override
    public UserInfo getUserInfo() throws DrivenException {
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
    public Result<DrivenException> authenticate(Context context)  {
        return authenticate(getSavedCredential(context));
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential) {
        Log.i(getName(), "Driven API is authenticating with GoogleDrive Service");
        googleDriveApi = null;
        userInfo = null;

        Result<DrivenException> result = new Result<DrivenException>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            if(credential.hasSavedCredential(getName())){
                credential.read(getName());
            }

            googleDriveApi = getGoogleDriveApiFactory().createApi(credential);
            userInfo = new GoogleDriveUser(googleDriveApi.about().get().setFields("name,user").execute());

            result.setSuccess(true);
            Log.i(getName(), "Driven API successfully authenticated by DriveUser: " + userInfo);

            // only save when it's not null
            if(credential.getAccountName() != null)
                credential.save(getName());
        }
        catch (IOException e){
            Log.i(getName(), "Driven API failed to authenticate");
            Log.e(getName(), "Exception:", e);
            result.setException(new DrivenException(e));
        }

        return result;
    }

    @Override
    public Result<DrivenException> clearSavedCredential(Context context) {
        Result<DrivenException> result = new Result<DrivenException>(false);
        googleDriveApi = null;
        userInfo = null;

        Credential credential = new Credential(context);
        credential.clear(getName());

        return result;
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
    public com.bingzer.android.driven.Permission getPermission(RemoteFile remoteFile) {
        try{
            PermissionList permissionList = getGoogleDriveApi().permissions().list(remoteFile.getId()).execute();
            return new GoogleDrivePermission(permissionList);
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public RemoteFile id(String id) {
        try{
            return new GoogleDriveFile(this, getGoogleDriveApi().files().get(id).setFields(defaultFields).execute(), false);
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public RemoteFile get(String name) {
        return getSearch().first("title = '" + name + "'");
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        return getSearch().first("'" + parent.getId() + "' in parents AND title = '" + name + "'");
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
            return new GoogleDriveFile(this, file, remoteFile.hasDetails());
        }
        catch (IOException e){
            return null;
        }
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
    public RemoteFile create(String name) {
        return create(null, name);
    }

    @Override
    public RemoteFile create(LocalFile local) {
        return create(null, local);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        try{
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(name);
            file.setMimeType(GoogleDriveFile.MIME_TYPE_FOLDER);

            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            file = getGoogleDriveApi().files().insert(file).execute();

            return id(file.getId());
        }
        catch (IOException e){
            return null;
        }
    }

    @Override
    public RemoteFile create(RemoteFile parent, LocalFile local) {
        try{
            com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
            file.setTitle(local.getName());

            if (parent != null)
                file.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));

            /////////////////////////////////////
            file = getGoogleDriveApi().files().insert(file, new com.google.api.client.http.FileContent(local.getType(), local.getFile())).execute();

            return id(file.getId());
        }
        catch (IOException e){
            return null;
        }
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
    public RemoteFile getDetails(RemoteFile remoteFile) {
        try{
            return new GoogleDriveFile(this, getGoogleDriveApi().files().get(remoteFile.getId()).execute(), true);
        }
        catch (IOException e){
            return null;
        }
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

    /**
     * Returns the "Search" interface
     */
    @Override
    public Search getSearch() {
        if(search == null)
            search = new SearchImpl();
        return search;
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

    /**
     * Returns the name of this provider
     */
    @Override
    public String getName() {
        return "GoogleDrive";
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
            list.add(new GoogleDriveFile(this, fileList.getItems().get(i), false));
        }

        return list;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    class SearchImpl extends AbsSearch {

        @Override
        public RemoteFile first(String query) {
            try{
                return GoogleDrive.this.first(query, defaultFieldsItems, true);
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
        public java.util.List<RemoteFile> query(String query) {
            try{
                return list(query, defaultFieldsItems, true);
            }
            catch (IOException e){
                return null;
            }
        }

        @Override
        public boolean isSupported() {
            return true;
        }
    }

    class SharingImpl extends AbsSharing {

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public String share(RemoteFile remoteFile, String user) {
            return share(remoteFile, user, com.bingzer.android.driven.Permission.PERMISSION_FULL);
        }

        @Override
        public String share(RemoteFile remoteFile, String user, int kind) {
            try{
                Permission permission = new Permission();

                permission.setValue(user);
                permission.setType("user");
                permission.setRole(getPermissionName(kind));

                permission = getGoogleDriveApi().permissions().insert(remoteFile.getId(), permission).execute();
                return permission.getSelfLink();
            }
            catch (IOException e){
                return null;
            }
        }
    }

    class SharedWithMeImpl extends AbsSharedWithMe {

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public boolean exists(String name) {
            return getSearch().first("title = '" + name + "' AND sharedWithMe") != null;
        }

        @Override
        public RemoteFile get(String name) {
            try {
                return GoogleDrive.this.first("title = '" + name + "' AND sharedWithMe", defaultFieldsItems, false);
            }
            catch (IOException e) {
                return null;
            }
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
    }

    class TrashedImpl extends AbsTrashed {

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
        public RemoteFile get(String name) {
            try {
                return GoogleDrive.this.first("'title' = " + name + "'", "id", true);
            }
            catch (IOException e){
                return null;
            }
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
    }

    class GoogleDriveUser extends DefaultUserInfo {

        protected GoogleDriveUser(About about){
            name = about.getName();
            displayName = about.getUser().getDisplayName();
            emailAddress = about.getUser().getEmailAddress();
        }

        protected GoogleDriveUser(String name, String displayName, String emailAddress){
            this.name = name;
            this.displayName = displayName;
            this.emailAddress = emailAddress;
        }
    }

    class GoogleDrivePermission extends AbsPermission {

        List<UserRole> userRoles = new ArrayList<UserRole>();

        protected GoogleDrivePermission(PermissionList permissionList){
            for(int i = 0; i < permissionList.getItems().size(); i++){
                Permission p = permissionList.getItems().get(i);

                UserInfo u = new GoogleDriveUser(p.getName(), p.getName(), p.getEmailAddress());
                int role = getPermissionValue(p.getRole());

                userRoles.add(new UserRole(u, role));
            }
        }

        @Override
        public List<UserRole> getRoles() {
            return userRoles;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    static String getPermissionName(int kind){
        switch (kind){
            default: return "writer";
            case com.bingzer.android.driven.Permission.PERMISSION_OWNER: return "owner";
            case com.bingzer.android.driven.Permission.PERMISSION_READ: return "read";
            case com.bingzer.android.driven.Permission.PERMISSION_FULL: return "writer";
        }
    }

    static int getPermissionValue(String role){
        if("owner".equalsIgnoreCase(role))
            return com.bingzer.android.driven.Permission.PERMISSION_OWNER;
        if("read".equalsIgnoreCase(role))
            return com.bingzer.android.driven.Permission.PERMISSION_READ;
        return com.bingzer.android.driven.Permission.PERMISSION_FULL;
    }

}
