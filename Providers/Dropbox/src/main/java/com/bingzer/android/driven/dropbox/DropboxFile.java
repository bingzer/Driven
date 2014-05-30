package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.Path;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

class DropboxFile implements RemoteFile {

    protected static StorageProvider storageProvider;
    protected static void setStorageProvider(StorageProvider storageProvider){
        DropboxFile.storageProvider = storageProvider;
    }
    protected static StorageProvider getStorageProvider(){
        if(storageProvider == null)
            storageProvider = new Dropbox();
        return storageProvider;
    }

    private String id;
    private String title;
    private String downloadUrl;
    private String type;
    private boolean isDirectory;
    private DropboxAPI.Entry model;

    DropboxFile(DropboxAPI.Entry model){
        init(model);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    public String getParentDirectory(){
        return Path.getDirectory(id);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean fetchDetails() {
        return hasDetails();
    }

    @Override
    public void fetchDetailsAsync(Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return fetchDetails();
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RemoteFile create(String name) {
        return getStorageProvider().create(this, name);
    }

    @Override
    public RemoteFile create(String name, LocalFile content) {
        return getStorageProvider().create(name, content);
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
    public void createAsync(final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(name);
            }
        });
    }

    @Override
    public RemoteFile get(String name) {
        return getStorageProvider().get(this, name);
    }

    @Override
    public void getAsync(final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return get(name);
            }
        });
    }

    @Override
    public List<RemoteFile> list() {
        return getStorageProvider().list(this);
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
    public boolean download(LocalFile local) {
        return getStorageProvider().download(this, local);
    }

    @Override
    public void downloadAsync(final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return download(local);
            }
        });
    }

    @Override
    public boolean upload(LocalFile local) {
        return consume(getStorageProvider().update(this, local));
    }

    @Override
    public void uploadAsync(final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return upload(local);
            }
        });
    }

    @Override
    public String share(String user) {
        return getStorageProvider().getSharing().share(this, user);
    }

    @Override
    public String share(String user, int kind) {
        return getStorageProvider().getSharing().share(this, user, kind);
    }

    @Override
    public void shareAsync(final String user, Task<String> task) {
        doAsync(task, new Delegate<String>() {
            @Override
            public String invoke() {
                return share(user);
            }
        });
    }

    @Override
    public void shareAsync(final String user, final int kind, Task<String> task) {
        doAsync(task, new Delegate<String>() {
            @Override public String invoke() {
                return share(user, kind);
            }
        });
    }

    @Override
    public boolean delete() {
        return getStorageProvider().delete(id);
    }

    @Override
    public void deleteAsync(Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return delete();
            }
        });
    }

    @Override
    public boolean rename(String name) {
        try {
            String newPath = Path.combine(getParentDirectory(), name);
            DropboxAPI.Entry entry = ((Dropbox) getStorageProvider()).getDropboxApi().move(id, newPath);
            return init(entry);
        }
        catch (DropboxException e){
            return false;
        }
    }

    @Override
    public void renameAsync(final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return rename(name);
            }
        });
    }

    @Override
    public String toString() {
        return getId();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean init(DropboxAPI.Entry entry){
        this.model = entry;
        id = entry.path;
        type = entry.mimeType;
        isDirectory = entry.isDir;
        title = entry.fileName();
        downloadUrl = entry.path;
        return true;
    }

    private boolean consume(RemoteFile remoteFile){
        if(!(remoteFile instanceof DropboxFile))
            return false;
        DropboxFile other = (DropboxFile) remoteFile;
        return init(other.model);
    }
}
