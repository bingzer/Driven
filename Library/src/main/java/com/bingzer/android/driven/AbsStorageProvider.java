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
package com.bingzer.android.driven;

import android.content.Context;

import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Search;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.contracts.Trashed;

import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * This is the base class that automatically implements all "Async" methods so you don't have to :)
 */
public abstract class AbsStorageProvider implements StorageProvider {

    protected Search search;
    protected Sharing sharing;
    protected SharedWithMe sharedWithMe;
    protected Trashed trashed;

    @Override
    public final boolean hasSavedCredential(Context context) {
        Credential credential = new Credential(context);
        return credential.hasSavedCredential(getName());
    }

    @Override
    public final Credential getSavedCredential(Context context) {
        Credential credential = new Credential(context);
        if(!credential.hasSavedCredential(getName()))
            throw new DrivenException("No saved credential");
        credential.read(getName());
        return credential;
    }

    @Override
    public Result<DrivenException> authenticate(Context context) {
        return authenticate(getSavedCredential(context));
    }

    /**
     * Async call for {@link #clearSavedCredential(android.content.Context)}
     */
    @Override
    public void clearSavedCredentialAsync(final Context context, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return clearSavedCredential(context);
            }
        });
    }

    /**
     * Async call for {@link #authenticate(android.content.Context)}
     */
    @Override
    public void authenticateAsync(final Context context, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return authenticate(context);
            }
        });
    }

    /**
     * Async call for {@link #authenticate(Credential)}
     */
    @Override
    public void authenticateAsync(final Credential credential, Task<Result<DrivenException>> task) {
        doAsync(task, new Delegate<Result<DrivenException>>() {
            @Override
            public Result<DrivenException> invoke() {
                return authenticate(credential);
            }
        });
    }

    /**
     * Async API for {@link #exists(String)}
     */
    @Override
    public void existsAsync(final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(name);
            }
        });
    }

    /**
     * Async call for {@link #exists(RemoteFile, String)}
     */
    @Override
    public void existsAsync(final RemoteFile parent, final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return exists(parent, name);
            }
        });
    }

    /**
     * Async call for {@link #get(RemoteFile, String)}
     */
    @Override
    public void getAsync(final RemoteFile parent, final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return get(parent, name);
            }
        });
    }

    /**
     * Async call fro {@link #get(String)}
     */
    @Override
    public void getAsync(final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return get(name);
            }
        });
    }

    /**
     * Async call for {@link #id(String)}
     */
    @Override
    public void idAsync(final String id, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return id(id);
            }
        });
    }

    /**
     * Async call for {@link #getDetails(RemoteFile)}
     */
    @Override
    public void getDetailsAsync(final RemoteFile remoteFile, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return getDetails(remoteFile);
            }
        });
    }

    /**
     * Async call for {@link #list(RemoteFile)}
     */
    @Override
    public void listAsync(final RemoteFile folder, Task<List<RemoteFile>> task) {
        doAsync(task, new Delegate<List<RemoteFile>>() {
            @Override
            public List<RemoteFile> invoke() {
                return list(folder);
            }
        });
    }

    /**
     * Async call for {@link #list()}
     */
    @Override
    public void listAsync(Task<List<RemoteFile>> task) {
        doAsync(task, new Delegate<List<RemoteFile>>() {
            @Override
            public List<RemoteFile> invoke() {
                return list();
            }
        });
    }

    /**
     * Async call for {@link StorageProvider#create(RemoteFile, LocalFile)}
     */
    @Override
    public void createAsync(final RemoteFile parent, final LocalFile local, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(parent, local);
            }
        });
    }

    /**
     * Async call for {@link #create(RemoteFile, String)}
     */
    @Override
    public void createAsync(final RemoteFile parent, final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(parent, name);
            }
        });
    }

    /**
     * Async call for {@link StorageProvider#create(LocalFile)}
     */
    @Override
    public void createAsync(final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(content);
            }
        });
    }

    /**
     * Async call for {@link #create(String)}
     */
    @Override
    public void createAsync(final String name, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return create(name);
            }
        });
    }

    /**
     * Async call for {@link #update(RemoteFile, LocalFile)}
     */
    @Override
    public void updateAsync(final RemoteFile remoteFile, final LocalFile content, Task<RemoteFile> task) {
        doAsync(task, new Delegate<RemoteFile>() {
            @Override
            public RemoteFile invoke() {
                return update(remoteFile, content);
            }
        });
    }

    /**
     * Async call for {@link #delete(String)}
     */
    @Override
    public void deleteAsync(final String id, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return delete(id);
            }
        });
    }

    /**
     * Async all for {@link #download(RemoteFile, LocalFile)}
     */
    @Override
    public void downloadAsync(final RemoteFile remoteFile, final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return download(remoteFile, local);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
