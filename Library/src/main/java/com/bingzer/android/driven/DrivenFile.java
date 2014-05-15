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

import com.bingzer.android.driven.contracts.Task;

import java.io.File;

/**
 * A wrapper for "File" in GoogleDrive side
 */
@SuppressWarnings("unused")
public interface DrivenFile {

    public String getId();

    public boolean isDirectory();

    public String getTitle();

    public String getType();

    public String getDownloadUrl();

    public boolean hasDetails();

    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean fetchDetails();

    public void fetchDetailsAsync(Task<Boolean> result);

    public Iterable<DrivenFile> list();

    public void listAsync(Task<Iterable<DrivenFile>> result);

    public File download(File local);

    public void downloadAsync(final File local, Task<File> result);

    public boolean upload(String mimeType, File content);

    public void uploadAsync(String mimeType, File content, Task<Boolean> result);

    public boolean share(String user);

    public void shareAsync(final String user, Task<Boolean> result);

    public boolean delete();

    public void deleteAsync(Task<Boolean> result);

}
