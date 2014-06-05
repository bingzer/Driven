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

import com.bingzer.android.driven.AbsRemoteFile;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.google.api.services.drive.model.File;

import java.util.List;

/**
 * A wrapper for "File" in GoogleDrive side
 */
@SuppressWarnings("unused")
class GoogleDriveFile extends AbsRemoteFile {
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    private String id;
    private String title;
    private String type;
    private String downloadUrl;
    private File fileModel;
    private boolean hasDetails;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected GoogleDriveFile(StorageProvider provider, File file, boolean hasDetails){
        super(provider);
        init(file, hasDetails);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isDirectory() {
        return MIME_TYPE_FOLDER.equals(fileModel.getMimeType());
    }

    @Override
    public String getName(){
        return title;
    }

    @Override
    public String getType(){
        return type;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public boolean hasDetails(){
        return hasDetails;
    }

    public File getModel(){
        return fileModel;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean fetchDetails(){
        if(!hasDetails) consume(getStorageProvider().getDetails(this));
        return true;
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
    public String share(String user){
        return getStorageProvider().getSharing().share(this, user);
    }

    @Override
    public String share(String user, int kind) {
        return getStorageProvider().getSharing().share(this, user, kind);
    }

    @Override
    public boolean delete(){
        return getStorageProvider().delete(getId());
    }

    @Override
    public boolean rename(String name) {
        fileModel.setTitle(name);
        return consume(getStorageProvider().update(this, null));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean init(File file, boolean details){
        fileModel = file;
        id = fileModel.getId();
        title = fileModel.getTitle();
        type = fileModel.getMimeType();
        downloadUrl = fileModel.getDownloadUrl();
        hasDetails = details;
        return true;
    }

    private boolean consume(RemoteFile remoteFile){
        if(!(remoteFile instanceof GoogleDriveFile))
            return false;
        GoogleDriveFile other = (GoogleDriveFile) remoteFile;
        return other.fileModel != null && init(other.fileModel, other.hasDetails);
    }
}
