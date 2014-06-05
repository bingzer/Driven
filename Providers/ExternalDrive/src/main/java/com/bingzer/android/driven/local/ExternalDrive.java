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
package com.bingzer.android.driven.local;

import android.content.Context;
import android.util.Log;

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
import com.bingzer.android.driven.contracts.Search;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Trashed;
import com.bingzer.android.driven.utils.IOUtils;
import com.bingzer.android.driven.utils.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class ExternalDrive extends AbsStorageProvider {

    private static File root;
    private static UserInfo userInfo;

    //////////////////////////////////////////////////////////////////////////////////////

    public File getRoot(){
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return root;
    }

    @Override
    public UserInfo getDrivenUser() {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return userInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return root != null && userInfo != null;
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential) {
        Log.i(getName(), "Driven API is authenticating with ExternalDrive Service");
        Result<DrivenException> result = new Result<DrivenException>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            if(credential.hasSavedCredential(getName())){
                credential.read(getName());
            }

            root = new File(credential.getAccountName());
            IOUtils.safeCreateDir(root);

            userInfo = new DefaultUserInfo();

            result.setSuccess(true);
            Log.i(getName(), "Driven API successfully authenticated by DriveUser: " + userInfo);

            credential.save(getName());
        }
        catch (Exception e) {
            Log.i(getName(), "Driven API failed to authenticate");
            Log.e(getName(), "Exception:", e);
            result.setException(new DrivenException(e));
        }

        return result;
    }

    @Override
    public Result<DrivenException> clearSavedCredential(Context context) {
        return new Result<DrivenException>();
    }

    @Override
    public boolean exists(String name) {
        return new File(getRoot(), name).exists();
    }

    @Override
    public boolean exists(RemoteFile parent, String name) {
        return new File(parent.getId(), name).exists();
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        File f = new File(Path.combine(parent, name));
        if(f.exists()){
            return new ExternalDriveFile(this, f.getAbsolutePath());
        }
        return null;
    }

    @Override
    public RemoteFile get(String name) {
        return get(new ExternalDriveFile(this, getRoot().getAbsolutePath()), name);
    }

    @Override
    public RemoteFile id(String id) {
        return new ExternalDriveFile(this, id);
    }

    @Override
    public RemoteFile getDetails(RemoteFile remoteFile) {
        return new ExternalDriveFile(this, remoteFile.getId());
    }

    @Override
    public List<RemoteFile> list() {
        return list(new ExternalDriveFile(this, getRoot().getAbsolutePath()));
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent) {
        if(parent == null) return list();

        File file = new File(parent.getId());
        List<RemoteFile> list = new ArrayList<RemoteFile>();
        File[] children = file.listFiles();
        if(children != null){
            for(File f : children){
                list.add(new ExternalDriveFile(this, f.getAbsolutePath()));
            }
        }
        return list;
    }

    @Override
    public RemoteFile create(String name) {
        return create(new ExternalDriveFile(this, getRoot().getAbsolutePath()), name);
    }

    @Override
    public RemoteFile create(LocalFile local) {
        return create(new ExternalDriveFile(this, getRoot().getAbsolutePath()), local);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        File f = new File(Path.combine(parent, name));
        IOUtils.safeCreateDir(f);
        return new ExternalDriveFile(this, f.getAbsolutePath());
    }

    @Override
    public RemoteFile create(RemoteFile parent, LocalFile local) {
        File f = new File(Path.combine(parent, local.getName()));
        try {
            IOUtils.copyFile(local.getFile(), f);
            return new ExternalDriveFile(this, f.getAbsolutePath());
        } catch (IOException e) {
            Log.e(getName(), "Create()", e);
            return null;
        }
    }

    @Override
    public RemoteFile update(RemoteFile remoteFile, LocalFile content) {
        // if name does not equal
        if(!remoteFile.getName().equalsIgnoreCase(content.getFile().getName())){
            // rename
            File from = new File(remoteFile.getId());
            File to = new File(from.getAbsolutePath(), content.getFile().getName());
            remoteFile = new ExternalDriveFile(this, to.getAbsolutePath());
        }

        // copy content
        try {
            IOUtils.copyFile(content.getFile(), new File(remoteFile.getId()));
            return remoteFile;
        }
        catch (IOException e) {
            Log.e(getName(), "update()", e);
            return null;
        }
    }

    @Override
    public boolean delete(String id) {
        File f = new File(id);
        if(f.isDirectory()){
            IOUtils.deleteTree(f, true);
            return true;
        }
        else return f.delete();
    }

    @Override
    public boolean download(RemoteFile remoteFile, LocalFile local) {
        File from = new File(remoteFile.getId());
        try {
            IOUtils.copyFile(from, local.getFile());
            return true;
        } catch (IOException e) {
            Log.e(getName(), "download()", e);
            return false;
        }
    }

    @Override
    public Search getSearch() {
        if (search == null)
            search = new SearchImpl();
        return search;
    }

    @Override
    public SharedWithMe getShared() {
        if (sharedWithMe == null)
            sharedWithMe = new SharedWithMeImpl();
        return sharedWithMe;
    }

    @Override
    public Sharing getSharing() {
        if (sharing == null)
            sharing = new SharingImpl();
        return sharing;
    }

    @Override
    public Trashed getTrashed() {
        if (trashed == null)
            trashed = new TrashedImpl();
        return trashed;
    }

    /**
     * Returns the name of this provider
     */
    @Override
    public String getName() {
        return "ExternalDrive";
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class SharedWithMeImpl extends AbsSharedWithMe {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RemoteFile get(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteFile> list() {
            throw new UnsupportedOperationException();
        }
    }

    class SharingImpl extends AbsSharing {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public String share(RemoteFile remoteFile, String user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String share(RemoteFile remoteFile, String user, int kind) {
            throw new UnsupportedOperationException();
        }

    }

    class TrashedImpl extends AbsTrashed {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RemoteFile get(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteFile> list() {
            throw new UnsupportedOperationException();
        }
    }

    class SearchImpl extends AbsSearch {

        @Override
        public RemoteFile first(String query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteFile> query(String query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSupported() {
            return false;
        }
    }

}
