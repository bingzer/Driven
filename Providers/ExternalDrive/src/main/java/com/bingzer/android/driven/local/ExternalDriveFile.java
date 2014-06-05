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

import android.webkit.MimeTypeMap;

import com.bingzer.android.driven.AbsRemoteFile;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.utils.IOUtils;

import java.io.File;
import java.util.List;

/**
 * A wrapper for "File" in GoogleDrive side
 */
@SuppressWarnings("unused")
class ExternalDriveFile extends AbsRemoteFile {

    private File file;

    protected ExternalDriveFile(StorageProvider provider, String path){
        super(provider);
        file = new File(path);
    }

    @Override
    public String getId() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getType() {
        String extension = IOUtils.getExtension(file);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public String getDownloadUrl() {
        return file.getAbsolutePath();
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
        RemoteFile remoteFile = getStorageProvider().update(this, local);
        if(remoteFile != null){
            file = new File(remoteFile.getId());
            return true;
        }
        return false;
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
        return getStorageProvider().delete(getId());
    }

    @Override
    public boolean rename(String name) {
        File newFile = new File(file.getParentFile().getAbsolutePath(), name);
        boolean renamed = file.renameTo(newFile);
        file = newFile;
        return renamed;
    }

}
