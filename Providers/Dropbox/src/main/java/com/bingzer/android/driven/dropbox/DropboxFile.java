package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.AbsRemoteFile;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.utils.Path;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.util.List;

class DropboxFile extends AbsRemoteFile {

    private String id;
    private String title;
    private String downloadUrl;
    private String type;
    private boolean isDirectory;
    private DropboxAPI.Entry model;

    DropboxFile(StorageProvider provider, DropboxAPI.Entry model){
        super(provider);
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


    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RemoteFile create(String name) {
        return getStorageProvider().create(this, name);
    }

    @Override
    public RemoteFile create(LocalFile content) {
        return getStorageProvider().create(this, content);
    }

    @Override
    public RemoteFile get(String name) {
        return getStorageProvider().get(this, name);
    }

    @Override
    public List<RemoteFile> list() {
        return getStorageProvider().list(this);
    }

    @Override
    public boolean download(LocalFile local) {
        return getStorageProvider().download(this, local);
    }

    @Override
    public boolean upload(LocalFile local) {
        return consume(getStorageProvider().update(this, local));
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
    public boolean delete() {
        return getStorageProvider().delete(id);
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
