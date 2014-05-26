package com.bingzer.android.driven.dropbox;

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
import com.bingzer.android.driven.utils.Path;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;
import static com.bingzer.android.driven.utils.IOUtils.safeClose;

public class Dropbox implements StorageProvider {
    private static final String TAG = "Dropbox";

    @Inject DropboxApiFactory apiFactory;
    private static DropboxAPI<AndroidAuthSession> dropboxApi;
    private static UserInfo userInfo;
    private SharedWithMe sharedWithMe;
    private Sharing sharing;
    private Trashed trashed;

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
    public UserInfo getDrivenUser() {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return userInfo;
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
    public boolean hasSavedCredentials(Context context) {
        Credential credential = new Credential(context);
        return credential.hasSavedCredential(TAG);
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential) {
        return authenticate(credential, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Result<DrivenException> authenticate(Credential credential, boolean saveCredential) {
        Log.i(TAG, "Driven API is authenticating with Dropbox Service");
        Result<DrivenException> result = new Result<DrivenException>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            if(credential.hasSavedCredential(TAG)){
                credential.read(TAG);
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
            Log.i(TAG, "Driven API successfully authenticated by DriveUser: " + userInfo);

            if(saveCredential)
                credential.save(TAG);
        }
        catch (Exception e) {
            Log.i(TAG, "Driven API failed to authenticate");
            Log.e(TAG, "Exception:", e);
            result.setException(new DrivenException(e));
        }

        return result;
    }

    @Override
    public void authenticateAsync(final Credential credential, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return authenticate(credential);
            }
        });
    }

    @Override
    public void authenticateAsync(final Credential credential, final boolean saveCredential, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return authenticate(credential, saveCredential);
            }
        });
    }

    @Override
    public Result<DrivenException> clearAuthentication(Context context) {
        Result<DrivenException> result = new Result<DrivenException>(false);
        dropboxApi = null;
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

    @Override
    public RemoteFile id(String id) {
        return get(id);
    }

    @Override
    public void idAsync(final String id, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return id(id);
            }
        });
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
    public void deleteAsync(final String id, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return delete(id);
            }
        });
    }

    @Override
    public RemoteFile getDetails(RemoteFile remoteFile) {
        return remoteFile;
    }

    @Override
    public void getDetailsAsync(final RemoteFile remoteFile, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return remoteFile;
            }
        });
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
    public void downloadAsync(final RemoteFile remoteFile, final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return download(remoteFile, local);
            }
        });
    }

    @Override
    public boolean exists(String name) {
        return get(name) != null;
    }

    @Override
    public boolean exists(RemoteFile parent, String name) {
        return get(parent, name) != null;
    }

    @Override
    public void existsAsync(final String title, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(title);
            }
        });
    }

    @Override
    public void existsAsync(final RemoteFile parent, final String title, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(parent, title);
            }
        });
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.combine(parent, name), 1, null, false, null);
            if(entry != null) return new DropboxFile(entry);
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
    public void getAsync(final RemoteFile parent, final String title, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return get(parent, title);
            }
        });
    }

    @Override
    public void getAsync(final String title, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return get(title);
            }
        });
    }

    @Override
    public List<RemoteFile> list() {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.ROOT, 0, null, true, null);

            List<RemoteFile> list = new ArrayList<RemoteFile>();
            if(entry != null && entry.contents != null){
                for(DropboxAPI.Entry children : entry.contents){
                    list.add(new DropboxFile(children));
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
                    list.add(new DropboxFile(children));
                }
            }
            return list;
        }
        catch (DropboxException e) {
            return null;
        }
    }

    @Override
    public void listAsync(final RemoteFile folder, Task<List<RemoteFile>> task) {
        doAsync(task, new Delegate<List<RemoteFile>>() {
            @Override
            public List<RemoteFile> invoke() {
                return list(folder);
            }
        });
    }

    @Override
    public void listAsync(Task<List<RemoteFile>> task) {
        doAsync(task, new Delegate<List<RemoteFile>>() {
            @Override
            public List<RemoteFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public RemoteFile create(String name) {
        return create(name, null);
    }

    @Override
    public RemoteFile create(String name, LocalFile content) {
        try {
            boolean isDirectory = content == null || content.getFile().isDirectory();
            if (isDirectory) {
                getDropboxApi().createFolder(Path.clean(name));
            }

            else{
                InputStream input = getApiFactory().createInputStream(content.getFile());
                getDropboxApi().putFile(Path.clean(name), input, content.getFile().length(), null, null);
                safeClose(input);
            }

            return get(name);
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        return create(parent, name, null);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name, LocalFile content) {
        return create(Path.combine(parent, name), content);
    }

    @Override
    public void createAsync(final RemoteFile parent, final String name, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(parent, name, content);
            }
        });
    }

    @Override
    public void createAsync(final RemoteFile parent, final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(parent, name);
            }
        });
    }

    @Override
    public void createAsync(final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(name);
            }
        });
    }

    @Override
    public void createAsync(final String name, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(name, content);
            }
        });
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

    @Override
    public void updateAsync(final RemoteFile remoteFile, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return update(remoteFile, content);
            }
        });
    }

    @Override
    public RemoteFile first(String query) {
        try {
            List<DropboxAPI.Entry> entryList = getDropboxApi().search(Path.ROOT, query, 1, true);
            return new DropboxFile(entryList.get(0));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void firstAsync(final String query, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return first(query);
            }
        });
    }

    @Override
    public List<RemoteFile> query(String query) {
        try {
            List<RemoteFile> list = new ArrayList<RemoteFile>();
            List<DropboxAPI.Entry> entryList = getDropboxApi().search(Path.ROOT, query, 0, true);
            for(DropboxAPI.Entry entry : entryList){
                list.add(new DropboxFile(entry));
            }

            return list;
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void queryAsync(final String query, Task<List<RemoteFile>> task) {
        doAsync(task, new Delegate<List<RemoteFile>>() {
            @Override
            public List<RemoteFile> invoke() {
                return query(query);
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    class SharedWithMeImpl implements SharedWithMe {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

        @Override
        public void existsAsync(final String name, Task<Boolean> result) {
            doAsync(result, new Delegate<Boolean>() {
                @Override
                public Boolean invoke() {
                    return exists(name);
                }
            });
        }

        @Override
        public RemoteFile get(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
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
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
        }

        @Override
        public void listAsync(Task<List<RemoteFile>> result) {
            doAsync(result, new Delegate<List<RemoteFile>>() {
                @Override
                public List<RemoteFile> invoke() {
                    return list();
                }
            });
        }
    }

    class TrashedImpl implements Trashed {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
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
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
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
            throw new DrivenException(new UnsupportedOperationException("Not supported"));
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

    class SharingImpl implements Sharing {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public String share(RemoteFile remoteFile, String user) {
            return share(remoteFile, user, PERMISSION_DEFAULT);
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
    }
}
