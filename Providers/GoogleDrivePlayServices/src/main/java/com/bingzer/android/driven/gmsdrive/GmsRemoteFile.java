package com.bingzer.android.driven.gmsdrive;

import com.bingzer.android.driven.AbsRemoteFile;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.google.android.gms.drive.DriveId;
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
        return provider.create(name);
    }

    @Override
    public RemoteFile create(LocalFile content) {
        return provider.create(content);
    }

    @Override
    public RemoteFile get(String name) {
        return provider.get(name);
    }

    @Override
    public List<RemoteFile> list() {
        return provider.list(this);
    }

    @Override
    public boolean download(LocalFile local) {
        return provider.download(this, local);
    }

    @Override
    public boolean upload(LocalFile local) {
        return consume(provider.update(this, local));
    }

    @Override
    public String share(String user) {
        return provider.getSharing().share(this, user);
    }

    @Override
    public String share(String user, int kind) {
        return provider.getSharing().share(this, user, kind);
    }

    @Override
    public boolean delete() {
        return provider.delete(getId());
    }

    @Override
    public boolean rename(String name) {
        throw new UnsupportedOperationException("rename");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public Metadata getMetadata(){
        return metadata;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private boolean consume(RemoteFile remoteFile){
        this.metadata = ((GmsRemoteFile) remoteFile).metadata;
        return true;
    }
}
