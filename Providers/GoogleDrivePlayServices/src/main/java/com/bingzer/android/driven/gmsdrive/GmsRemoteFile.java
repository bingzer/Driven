package com.bingzer.android.driven.gmsdrive;

import com.bingzer.android.driven.AbsRemoteFile;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.google.android.gms.drive.Metadata;

import java.util.List;

class GmsRemoteFile extends AbsRemoteFile{
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    private Metadata metadata;

    protected GmsRemoteFile(StorageProvider provider, Metadata metadata){
        super(provider);
        this.metadata = metadata;
    }

    @Override
    public String getId() {
        return metadata.getDriveId().toString();
    }

    @Override
    public boolean isDirectory() {
        return MIME_TYPE_FOLDER.equals(metadata.getMimeType());
    }

    @Override
    public String getName() {
        return metadata.getTitle();
    }

    @Override
    public String getType() {
        return metadata.getMimeType();
    }

    @Override
    public String getDownloadUrl() {
        return metadata.getWebViewLink();
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    @Override
    public boolean fetchDetails() {
        return true;
    }

    @Override
    public RemoteFile create(String name) {
        return null;
    }

    @Override
    public RemoteFile create(LocalFile content) {
        return null;
    }

    @Override
    public RemoteFile get(String name) {
        return null;
    }

    @Override
    public List<RemoteFile> list() {
        return null;
    }

    @Override
    public boolean download(LocalFile local) {
        return false;
    }

    @Override
    public boolean upload(LocalFile local) {
        return false;
    }

    @Override
    public String share(String user) {
        return null;
    }

    @Override
    public String share(String user, int kind) {
        return null;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean rename(String name) {
        return false;
    }
}
