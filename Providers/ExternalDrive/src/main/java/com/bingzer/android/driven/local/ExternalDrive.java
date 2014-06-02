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

import android.content.Context;
import android.util.Log;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.UserInfo;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.contracts.Trashed;
import com.bingzer.android.driven.utils.IOUtils;
import com.bingzer.android.driven.utils.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * Driven
 */
@SuppressWarnings("unused")
public final class ExternalDrive implements StorageProvider {

    private static final String TAG = "ExternalDrive";
    private final File root;
    private UserInfo userInfo = new ExternalDriveUserInfo();

    /**
     * Creates an external drive with path root
     * @param path the root
     */
    public ExternalDrive(String path){
        root = new File(path);
        IOUtils.safeCreateDir(root);

        ExternalDriveFile.setStorageProvider(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public UserInfo getDrivenUser() {
        return userInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean hasSavedCredentials(Context context) {
        return true;
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential) {
        return authenticate(credential, true);
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential, boolean saveCredential) {
        return new Result<DrivenException>();
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
        return new Result<DrivenException>();
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
    public boolean exists(String name) {
        return new File(root, name).exists();
    }

    @Override
    public boolean exists(RemoteFile parent, String name) {
        return new File(parent.getId(), name).exists();
    }

    @Override
    public void existsAsync(final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(name);
            }
        });
    }

    @Override
    public void existsAsync(final RemoteFile parent, final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(parent, name);
            }
        });
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        File f = new File(Path.combine(parent, name));
        if(f.exists()){
            return new ExternalDriveFile(f.getAbsolutePath());
        }
        return null;
    }

    @Override
    public RemoteFile get(String name) {
        return get(new ExternalDriveFile(root.getAbsolutePath()), name);
    }

    @Override
    public void getAsync(final RemoteFile parent, final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return get(parent, name);
            }
        });
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
    public RemoteFile id(String id) {
        return new ExternalDriveFile(id);
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
    public RemoteFile getDetails(RemoteFile remoteFile) {
        return new ExternalDriveFile(remoteFile.getId());
    }

    @Override
    public void getDetailsAsync(final RemoteFile remoteFile, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return getDetails(remoteFile);
            }
        });
    }

    @Override
    public List<RemoteFile> list() {
        return list(new ExternalDriveFile(root.getAbsolutePath()));
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent) {
        File file = new File(parent.getId());
        List<RemoteFile> list = new ArrayList<RemoteFile>();
        for(String absolutePath : file.list()){
            list.add(new ExternalDriveFile(absolutePath));
        }
        return list;
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
        return create(new ExternalDriveFile(root.getAbsolutePath()), name);
    }

    @Override
    public RemoteFile create(String name, LocalFile content) {
        return create(new ExternalDriveFile(root.getAbsolutePath()), name, content);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        File f = new File(Path.combine(parent, name));
        IOUtils.safeCreateDir(f);
        return new ExternalDriveFile(f.getAbsolutePath());
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name, LocalFile content) {
        File f = new File(Path.combine(parent, name));
        try {
            IOUtils.copyFile(content.getFile(), f);
            return new ExternalDriveFile(f.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Create()", e);
            return null;
        }
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
    public RemoteFile update(RemoteFile remoteFile, LocalFile content) {
        // if name does not equal
        if(!remoteFile.getName().equalsIgnoreCase(content.getFile().getName())){
            // rename
            File from = new File(remoteFile.getId());
            File to = new File(from.getAbsolutePath(), content.getFile().getName());
            remoteFile = new ExternalDriveFile(to.getAbsolutePath());
        }

        // copy content
        try {
            IOUtils.copyFile(content.getFile(), new File(remoteFile.getId()));
            return remoteFile;
        }
        catch (IOException e) {
            Log.e(TAG, "update()", e);
            return null;
        }
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
    public boolean delete(String id) {
        File f = new File(id);
        if(f.isDirectory()){
            IOUtils.deleteTree(f, true);
            return true;
        }
        else return f.delete();
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
    public RemoteFile first(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void firstAsync(String query, Task<RemoteFile> task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RemoteFile> query(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void queryAsync(String query, Task<List<RemoteFile>> task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean download(RemoteFile remoteFile, LocalFile local) {
        File from = new File(remoteFile.getId());
        try {
            IOUtils.copyFile(from, local.getFile());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "download()", e);
            return false;
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
    public SharedWithMe getShared() {
        return new SharedWithMeImpl();
    }

    @Override
    public Sharing getSharing() {
        return new SharingImpl();
    }

    @Override
    public Trashed getTrashed() {
        return new TrashedImpl();
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class SharedWithMeImpl implements SharedWithMe {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void existsAsync(String name, Task<Boolean> result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RemoteFile get(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void getAsync(String name, Task<RemoteFile> result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteFile> list() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void listAsync(Task<List<RemoteFile>> result) {
            throw new UnsupportedOperationException();
        }
    }

    class SharingImpl implements Sharing {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public String share(RemoteFile remoteFile, String user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String share(RemoteFile remoteFile, String user, int kind) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shareAsync(RemoteFile remoteFile, String user, Task<String> result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shareAsync(RemoteFile remoteFile, String user, int kind, Task<String> result) {
            throw new UnsupportedOperationException();
        }
    }

    class TrashedImpl implements Trashed {

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean exists(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void existsAsync(String name, Task<Boolean> result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RemoteFile get(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void getAsync(String name, Task<RemoteFile> result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<RemoteFile> list() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void listAsync(Task<List<RemoteFile>> result) {
            throw new UnsupportedOperationException();
        }
    }

}
