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
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

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
    private boolean hasDetails;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected DriveFile(File file, boolean hasDetails){
        init(file, hasDetails);
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

    public boolean hasDetails(){
        return hasDetails;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    public File getDetails(){
        if(!hasDetails) consume(Driven.getDriven().getDetails(this));
        return fileModel;
    }

    public void getDetailsAsync(Task<File> result){
        doAsync(result, new Delegate<File>() {
            @Override public File invoke() {
                return getDetails();
            }
        });
    }

    public Iterable<DriveFile> list() {
        return Driven.getDriven().list(this);
    }

    public void listAsync(Task<Iterable<DriveFile>> result) {
        doAsync(result, new Delegate<Iterable<DriveFile>>() {
            @Override public Iterable<DriveFile> invoke() {
                return list();
            }
        });
    }

    public java.io.File download(java.io.File local) {
        return Driven.getDriven().download(this, local);
    }

    public void downloadAsync(final java.io.File local, Task<java.io.File> result) {
        doAsync(result, new Delegate<java.io.File>() {
            @Override public java.io.File invoke() {
                return download(local);
            }
        });
    }

    public boolean upload(FileContent content) {
        return consume(Driven.getDriven().update(this, content));
    }

    public void uploadAsync(final FileContent content, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return upload(content);
            }
        });
    }

    public boolean share(String user){
        return Driven.getDriven().share(this, user);
    }

    public void shareAsync(Task<Boolean> result, final String user){
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return share(user);
            }
        });
    }

    public boolean delete(){
        return Driven.getDriven().delete(getId());
    }

    public void deleteAsync(Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return delete();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean init(File file, boolean details){
        fileModel = file;
        id = fileModel.getId();
        title = fileModel.getTitle();
        type = fileModel.getMimeType();
        hasDetails = details;
        return true;
    }

    private boolean consume(DriveFile other){
        return other != null && other.fileModel != null && init(other.fileModel, other.hasDetails);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static Iterable<DriveFile> from(FileList fileList){
        List<DriveFile> list = new ArrayList<DriveFile>();
        for(int i = 0; i < fileList.size(); i++){
            list.add(new DriveFile(fileList.getItems().get(i), false));
        }

        return list;
    }
}
