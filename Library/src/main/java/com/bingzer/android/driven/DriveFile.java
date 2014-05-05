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
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

/**
 * Created by Ricky on 5/3/2014.
 */
@SuppressWarnings("unused")
public class DriveFile {
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    ///////////////////////////////////////////////////////////////////////////////////////////
    private String id;
    private String title;
    private String type;
    private File fileModel;
    ///////////////////////////////////////////////////////////////////////////////////////////

    public DriveFile(File file){
        fileModel = file;

        id = fileModel.getId();
        title = fileModel.getTitle();
        type = fileModel.getMimeType();
    }

    public String getId() {
        return id;
    }

    public boolean isDirectory() {
        return MIME_TYPE_FOLDER.equals(fileModel.getMimeType());
    }

    public String getTitle(){
        return title;
    }

    public String getType(){
        return type;
    }

    public File getModel(){
        return fileModel;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean loadDetails(){
        DriveFile file = Driven.getDriven().getDetails(this);
        if(file != null && file.fileModel != null){
            fileModel = file.fileModel;
            return true;
        }

        return false;
    }

    public void loadDetailsAsync(Task<Boolean> result){
        Driven.getDriven().doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return loadDetails();
            }
        });
    }

    public Iterable<DriveFile> list() {
        return Driven.getDriven().list(this);
    }

    public void listAsync(Task<Iterable<DriveFile>> result) {
        Driven.getDriven().doAsync(result, new Delegate<Iterable<DriveFile>>() {
            @Override public Iterable<DriveFile> invoke() {
                return list();
            }
        });
    }

    public java.io.File download(java.io.File local) {
        return Driven.getDriven().download(this, local);
    }

    public void downloadAsync(final java.io.File local, Task<java.io.File> result) {
        Driven.getDriven().doAsync(result, new Delegate<java.io.File>() {
            @Override public java.io.File invoke() {
                return download(local);
            }
        });
    }

    public DriveFile upload(FileContent content) {
        return Driven.getDriven().update(this, content);
    }

    public void uploadAsync(final FileContent content, Task<DriveFile> result) {
        Driven.getDriven().doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return upload(content);
            }
        });
    }

    public DriveFile share(String... users){
        throw new UnsupportedOperationException("Not implemented");
    }

    public void shareAsync(Task<DriveFile> result, final String... users){
        Driven.getDriven().doAsync(result, new Delegate<DriveFile>() {
            @Override public DriveFile invoke() {
                return share(users);
            }
        });
    }
}
