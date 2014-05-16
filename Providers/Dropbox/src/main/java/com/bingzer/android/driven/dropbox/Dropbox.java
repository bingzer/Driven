package com.bingzer.android.driven.dropbox;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.DrivenUser;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;
import static com.bingzer.android.driven.utils.IOUtils.safeClose;

public class Dropbox implements Driven {
    private static final String TAG = "Dropbox";

    @Inject DropboxApiFactory apiFactory;
    private DropboxAPI<AndroidAuthSession> dropboxApi;
    private DrivenUser drivenUser;

    ////////////////////////////////////////////////////////////////////////////////////////////

    public DropboxAPI<AndroidAuthSession> getDropboxApi(){
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
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
    public DrivenUser getDrivenUser() {
        if(!isAuthenticated()) throw new DrivenException("Driven API is not yet authenticated. Call authenticate() first");
        return drivenUser;
    }

    @Override
    public boolean isAuthenticated() {
        return dropboxApi != null && drivenUser != null;
    }

    @Override
    public Result<DrivenException> authenticate(DrivenCredential credential) {
        return authenticate(credential, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Result<DrivenException> authenticate(DrivenCredential credential, boolean saveCredential) {
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        try {
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
            drivenUser = new DropboxUser(dropboxApi.accountInfo());

            if(saveCredential)
                credential.save(TAG);

            result.setSuccess(true);
        }
        catch (Exception e) {
            result.setException(new DrivenException(e));
        }

        return result;
    }

    @Override
    public void authenticateAsync(final DrivenCredential credential, Task<Result<DrivenException>> result) {
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return authenticate(credential);
            }
        });
    }

    @Override
    public void authenticateAsync(final DrivenCredential credential, final boolean saveCredential, Task<Result<DrivenException>> result) {
        doAsync(result, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return authenticate(credential, saveCredential);
            }
        });
    }

    @Override
    public Result<DrivenException> deauthenticate(Context context) {
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        dropboxApi = null;
        drivenUser = null;

        DrivenCredential credential = new DrivenCredential(context);
        if(credential.hasSavedCredential(TAG)){
            result.setSuccess(credential.read(TAG));
        }

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

    @Override
    public DrivenFile id(String id) {
        return get(id);
    }

    @Override
    public void idAsync(final String id, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
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
    public void deleteAsync(final String id, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return delete(id);
            }
        });
    }

    @Override
    public DrivenFile getDetails(DrivenFile drivenFile) {
        return drivenFile;
    }

    @Override
    public void getDetailsAsync(final DrivenFile drivenFile, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return drivenFile;
            }
        });
    }

    @Override
    public File download(DrivenFile drivenFile, File local) {
        OutputStream output = null;
        try {
            output = getApiFactory().createOutputStream(local);
            getDropboxApi().getFile(Path.clean(drivenFile), null, output, null);
            return local;
        }
        catch (Exception e) {
            return null;
        }
        finally {
            safeClose(output);
        }
    }

    @Override
    public void downloadAsync(final DrivenFile drivenFile, final File local, Task<File> result) {
        doAsync(result, new Delegate<File>() {
            @Override
            public File invoke() {
                return download(drivenFile, local);
            }
        });
    }

    @Override
    public boolean exists(String title) {
        return get(title) != null;
    }

    @Override
    public boolean exists(DrivenFile parent, String title) {
        return get(parent, title) != null;
    }

    @Override
    public void existsAsync(final String title, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(title);
            }
        });
    }

    @Override
    public void existsAsync(final DrivenFile parent, final String title, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(parent, title);
            }
        });
    }

    @Override
    public DrivenFile get(DrivenFile parent, String title) {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.combine(parent, title), 1, null, false, null);
            if(entry != null) return new DropboxFile(entry);
            return null;
        }
        catch (DropboxException e) {
            return null;
        }
    }

    @Override
    public DrivenFile get(String title) {
        return get(null, title);
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

    @Override
    public Iterable<DrivenFile> list() {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.ROOT, 0, null, true, null);

            List<DrivenFile> list = new ArrayList<DrivenFile>();
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
    public Iterable<DrivenFile> list(DrivenFile folder) {
        try {
            DropboxAPI.Entry entry = getDropboxApi().metadata(Path.clean(folder), 0, null, true, null);

            List<DrivenFile> list = new ArrayList<DrivenFile>();
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
    public void listAsync(final DrivenFile folder, Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override
            public Iterable<DrivenFile> invoke() {
                return list(folder);
            }
        });
    }

    @Override
    public void listAsync(Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override
            public Iterable<DrivenFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public DrivenFile create(String name) {
        return create(name, null);
    }

    @Override
    public DrivenFile create(String name, DrivenContent content) {
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
    public DrivenFile create(DrivenFile parent, String name) {
        return create(parent, name, null);
    }

    @Override
    public DrivenFile create(DrivenFile parent, String name, DrivenContent content) {
        return create(Path.combine(parent, name), content);
    }

    @Override
    public void createAsync(final DrivenFile parent, final String name, final DrivenContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return create(parent, name, content);
            }
        });
    }

    @Override
    public void createAsync(final DrivenFile parent, final String name, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return create(parent, name);
            }
        });
    }

    @Override
    public void createAsync(final String name, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return create(name);
            }
        });
    }

    @Override
    public void createAsync(final String name, final DrivenContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return create(name, content);
            }
        });
    }

    @Override
    public DrivenFile update(DrivenFile drivenFile, DrivenContent content) {
        InputStream input = null;
        try{
            input = getApiFactory().createInputStream(content.getFile());
            getDropboxApi().putFileOverwrite(Path.clean(drivenFile), input, content.getFile().length(), null);
            return drivenFile;
        }
        catch (Exception e){
            safeClose(input);
        }

        return null;
    }

    @Override
    public void updateAsync(final DrivenFile drivenFile, final DrivenContent content, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return update(drivenFile, content);
            }
        });
    }

    @Override
    public DrivenFile first(String query) {
        try {
            List<DropboxAPI.Entry> entryList = getDropboxApi().search(Path.ROOT, query, 1, true);
            return new DropboxFile(entryList.get(0));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void firstAsync(final String query, Task<DrivenFile> result) {
        doAsync(result, new Delegate<DrivenFile>() {
            @Override
            public DrivenFile invoke() {
                return first(query);
            }
        });
    }

    @Override
    public Iterable<DrivenFile> query(String query) {
        try {
            List<DrivenFile> list = new ArrayList<DrivenFile>();
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
    public void queryAsync(final String query, Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override
            public Iterable<DrivenFile> invoke() {
                return query(query);
            }
        });
    }

    @Override
    public boolean share(DrivenFile drivenFile, String user) {
        try {
            DropboxAPI.DropboxLink link = getDropboxApi().share(Path.clean(drivenFile));
            // TODO: send email here
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public void shareAsync(final DrivenFile drivenFile, final String user, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return share(drivenFile, user);
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

}
