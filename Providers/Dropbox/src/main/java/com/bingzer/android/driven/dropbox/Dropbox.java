package com.bingzer.android.driven.dropbox;

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
import com.bingzer.android.driven.Permission;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.UserInfo;
import com.bingzer.android.driven.UserRole;
import com.bingzer.android.driven.contracts.Search;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Trashed;
import com.bingzer.android.driven.utils.Path;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static com.bingzer.android.driven.utils.IOUtils.safeClose;

public class Dropbox extends AbsStorageProvider {
    @Inject DropboxApiFactory apiFactory;
    private static DropboxAPI<AndroidAuthSession> dropboxApi;
    private static UserInfo userInfo;

    ////////////////////////////////////////////////////////////////////////////////////////////

    public DropboxAPI<AndroidAuthSession> getDropboxApi(){
        if(dropboxApi == null) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return dropboxApi;
    }

    public DropboxApiFactory getApiFactory(){
        // if it's not injected.. create the default one
        if(apiFactory == null){
            apiFactory = new DropboxApiFactory.Default();
        }

        return apiFactory;
    }

    @Override
    public UserInfo getUserInfo() {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return userInfo;
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
    public SharedWithMe getShared() {
        if(sharedWithMe == null)
            sharedWithMe = new SharedWithMeImpl();
        return sharedWithMe;
    }

    @Override
    public Trashed getTrashed() {
        if(trashed == null)
            trashed = new TrashedImpl();
        return trashed;
    }

    /**
     * Returns the name of this provider
     */
    @Override
    public String getName() {
        return "Dropbox";
    }

    public Sharing getSharing(){
        if(sharing == null)
            sharing = new SharingImpl();
        return sharing;
    }

    @Override
    public boolean isAuthenticated() {
        return dropboxApi != null && userInfo != null;
    }

    @Override
    public Result<DrivenException> authenticate(Context context) {
        return authenticate(getSavedCredential(context));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Result<DrivenException> authenticate(Credential credential) {
        Log.i(getName(), "Driven API is authenticating with Dropbox Service");
        Result<DrivenException> result = new Result<DrivenException>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            if(credential.hasSavedCredential(getName())){
                credential.read(getName());
            }

            // And later in some initialization function:
            AppKeyPair appKeys = new AppKeyPair(credential.getToken().getApplicationKey(), credential.getToken().getApplicationSecret());
            AndroidAuthSession session = new AndroidAuthSession(appKeys);

            // only if it's non null
            // then set
            if(credential.getToken().getAccessToken() != null)
                session.setOAuth2AccessToken(credential.getToken().getAccessToken());

            dropboxApi = getApiFactory().createApi(session);
            userInfo = new DropboxUserInfo(dropboxApi.accountInfo());

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
        Result<DrivenException> result = new Result<DrivenException>(false);
        dropboxApi = null;
        userInfo = null;

        Credential credential = new Credential(context);
        credential.clear(getName());

        return result;
    }

    @Override
    public RemoteFile id(String id) {
        return get(id);
    }

    @Override
    public boolean delete(String id) {
        try {
            getDropboxApi().delete(Path.clean(id));
            return true;
        }
        catch (DropboxException e) {
            return false;
        }
    }

    @Override
    public RemoteFile getDetails(RemoteFile remoteFile) {
        return remoteFile;
    }

    @Override
    public boolean download(RemoteFile remoteFile, LocalFile local) {
        OutputStream output = null;
        try {
            output = getApiFactory().createOutputStream(local.getFile());
            DropboxAPI.DropboxFileInfo fileInfo = getDropboxApi().getFile(Path.clean(remoteFile), null, output, null);
            return fileInfo != null;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            safeClose(output);
        }
    }

    @Override
    public boolean exists(String name) {
        return get(name) != null;
    }

    @Override
    public boolean exists(RemoteFile parent, String name) {
        return get(parent, name) != null;
    }

    /**
     * Returns the role that {@link #getUserInfo()} has for the
     * specified {@code remoteFile}.
     */
    @Override
    public Permission getPermission(RemoteFile remoteFile) {
        return new AbsPermission() {
            @Override
            public List<UserRole> getRoles() {
                return Arrays.asList(new UserRole(getUserInfo(), PERMISSION_OWNER));
            }
        };
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.combine(parent, name), 1, null, false, null);
            if(entry != null) return new DropboxFile(this, entry);
            return null;
        }
        catch (DropboxException e) {
            return null;
        }
    }

    @Override
    public RemoteFile get(String name) {
        return get(null, name);
    }

    @Override
    public List<RemoteFile> list() {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.ROOT, 0, null, true, null);

            List<RemoteFile> list = new ArrayList<RemoteFile>();
            if(entry != null && entry.contents != null){
                for(DropboxAPI.Entry children : entry.contents){
                    list.add(new DropboxFile(this, children));
                }
            }

            return list;
        }
        catch (DropboxException e) {
            return null;
        }
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent) {
        if(parent == null) return list();

        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.clean(parent), 0, null, true, null);

            List<RemoteFile> list = new ArrayList<RemoteFile>();
            if(entry != null && entry.contents != null){
                for(DropboxAPI.Entry children : entry.contents){
                    list.add(new DropboxFile(this, children));
                }
            }
            return list;
        }
        catch (DropboxException e) {
            return null;
        }
    }

    @Override
    public RemoteFile create(String name) {
        try {
            if(name == null) throw new NullPointerException("name");
            getDropboxApi().createFolder(Path.clean(name));

            return get(name);
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public RemoteFile create(LocalFile local) {
        try {
            if(local.getName() == null) throw new NullPointerException("LocalFile.getName()");

            InputStream input = getApiFactory().createInputStream(local.getFile());
            getDropboxApi().putFile(Path.clean(local.getName()), input, local.getFile().length(), null, null);
            safeClose(input);

            return get(local.getName());
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        return create(Path.combine(parent, name));
    }

    @Override
    public RemoteFile create(RemoteFile parent, LocalFile local) {
        return create(local);
    }

    @Override
    public RemoteFile update(RemoteFile remoteFile, LocalFile content) {
        InputStream input = null;
        try{
            input = getApiFactory().createInputStream(content.getFile());
            getDropboxApi().putFileOverwrite(Path.clean(remoteFile), input, content.getFile().length(), null);
            return remoteFile;
        }
        catch (Exception e){
            safeClose(input);
        }

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    class SearchImpl extends AbsSearch {

        @Override
        public RemoteFile first(String query) {
            try {
                List<DropboxAPI.Entry> entryList = getDropboxApi().search(Path.ROOT, query, 1, true);
                return new DropboxFile(Dropbox.this, entryList.get(0));
            }
            catch (Exception e){
                return null;
            }
        }

        @Override
        public List<RemoteFile> query(String query) {
            try {
                List<RemoteFile> list = new ArrayList<RemoteFile>();
                List<DropboxAPI.Entry> entryList = getDropboxApi().search(Path.ROOT, query, 0, true);
                for(DropboxAPI.Entry entry : entryList){
                    list.add(new DropboxFile(Dropbox.this, entry));
                }

                return list;
            }
            catch (Exception e){
                return null;
            }
        }

        @Override
        public boolean isSupported() {
            return true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    class SharedWithMeImpl extends AbsSharedWithMe {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

        @Override
        public RemoteFile get(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

        @Override
        public List<RemoteFile> list() {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

    }

    class TrashedImpl extends AbsTrashed {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

        @Override
        public RemoteFile get(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

        @Override
        public List<RemoteFile> list() {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

    }

    class SharingImpl extends AbsSharing {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public String share(RemoteFile remoteFile, String user) {
            return share(remoteFile, user, Permission.PERMISSION_FULL);
        }

        @Override
        public String share(RemoteFile remoteFile, String user, int kind) {
            try {
                DropboxAPI.DropboxLink link = getDropboxApi().share(remoteFile.getId());
                return link.url;
            }
            catch (Exception e) {
                return null;
            }
        }

        @Override
        public boolean removeSharing(RemoteFile remoteFile, String user) {
            // TODO: remove sharing from DropBox
            return false;
        }

    }

    class DropboxUserInfo extends DefaultUserInfo {

        DropboxUserInfo(DropboxAPI.Account account){
            name = account.displayName;
            displayName = account.displayName;
        }

    }
}
