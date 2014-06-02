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

import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.IOUtils;

import java.io.File;
import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * A wrapper for "File" in GoogleDrive side
 */
@SuppressWarnings("unused")
class ExternalDriveFile implements RemoteFile {

    private static final String TAG = "ExternalDriveFile";
    protected static StorageProvider storageProvider;
    protected static void setStorageProvider(StorageProvider storageProvider){
        ExternalDriveFile.storageProvider = storageProvider;
    }
    protected static StorageProvider getStorageProvider(){
        return storageProvider;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private File file;

    protected ExternalDriveFile(String path){
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
    public void fetchDetailsAsync(Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return fetchDetails();
            }
        });
    }

    @Override
    public RemoteFile create(String name) {
        return getStorageProvider().create(this, name);
    }

    @Override
    public RemoteFile create(String name, LocalFile content) {
        return getStorageProvider().create(this, name, content);
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
        RemoteFile remoteFile = getStorageProvider().update(this, local);
        if(remoteFile != null){
            file = new File(remoteFile.getId());
            return true;
        }
        return false;
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
            @Override
            public String invoke() {
                return share(user, kind);
            }
        });
    }

    @Override
    public boolean delete() {
        return getStorageProvider().delete(getId());
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
        File newFile = new File(file.getParentFile().getAbsolutePath(), name);
        boolean renamed = file.renameTo(newFile);
        file = newFile;
        return renamed;
    }

    @Override
    public void renameAsync(final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return rename(name);
            }
        });
    }
}
