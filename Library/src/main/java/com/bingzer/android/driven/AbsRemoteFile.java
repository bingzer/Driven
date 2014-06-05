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

import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;

import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * Abstract impl that implements all "Async" methods
 */
public abstract class AbsRemoteFile implements RemoteFile {

    protected StorageProvider provider;

    /**
     * Creates an instance of RemoteFile
     */
    protected AbsRemoteFile(StorageProvider provider){
        this.provider = provider;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Async for {@link #fetchDetails()}
     */
    @Override
    public void fetchDetailsAsync(Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return fetchDetails();
            }
        });
    }

    /**
     * Async call for {@link #create(LocalFile)}
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
     * Async for {@link #get(String)}
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
     * Async for {@link #list()}
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
     * Async for {@link #download(LocalFile)}
     */
    @Override
    public void downloadAsync(final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return download(local);
            }
        });
    }

    /**
     * Async for {@link #upload(com.bingzer.android.driven.LocalFile)}
     */
    @Override
    public void uploadAsync(final LocalFile local, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return upload(local);
            }
        });
    }

    /**
     * Async for {@link #share(String)}
     */
    @Override
    public void shareAsync(final String user, Task<String> task) {
        doAsync(task, new Delegate<String>() {
            @Override
            public String invoke() {
                return share(user);
            }
        });
    }

    /**
     * Async for {@link #share(String, int)}
     */
    @Override
    public void shareAsync(final String user, final int kind, Task<String> task) {
        doAsync(task, new Delegate<String>() {
            @Override
            public String invoke() {
                return share(user, kind);
            }
        });
    }

    /**
     * Async for {@link #delete()}
     */
    @Override
    public void deleteAsync(Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return delete();
            }
        });
    }

    /**
     * Async call for {@link #rename(String)}
     */
    @Override
    public void renameAsync(final String name, Task<Boolean> task) {
        doAsync(task, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return rename(name);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the storage provider that creates this RemoteFile
     */
    protected StorageProvider getStorageProvider(){
        return provider;
    }

}
