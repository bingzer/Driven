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

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;
import com.google.api.services.drive.model.File;

import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * A wrapper for "File" in GoogleDrive side
 */
@SuppressWarnings("unused")
class GoogleDriveFile implements DrivenFile {
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    protected static Driven driven;
    protected static void setDriven(Driven driven){
        GoogleDriveFile.driven = driven;
    }

    protected static Driven getDriven(){
        if(driven == null)
            driven = new GoogleDrive();
        return driven;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    private String id;
    private String title;
    private String type;
    private String downloadUrl;
    private File fileModel;
    private boolean hasDetails;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected GoogleDriveFile(File file, boolean hasDetails){
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
        if(!hasDetails) consume(getDriven().getDetails(this));
        return true;
    }

    @Override
    public void fetchDetailsAsync(Task<Boolean> result){
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return fetchDetails();
            }
        });
    }

    @Override
    public List<DrivenFile> list() {
        return getDriven().list(this);
    }

    @Override
    public void listAsync(Task<List<DrivenFile>> result) {
        doAsync(result, new Delegate<List<DrivenFile>>() {
            @Override public List<DrivenFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public DrivenContent download(java.io.File local) {
        return getDriven().download(this, local);
    }

    @Override
    public void downloadAsync(final java.io.File local, Task<DrivenContent> result) {
        doAsync(result, new Delegate<DrivenContent>() {
            @Override public DrivenContent invoke() {
                return download(local);
            }
        });
    }

    @Override
    public boolean upload(String mimeType, java.io.File content) {
        return consume(getDriven().update(this, new DrivenContent(mimeType, content)));
    }

    @Override
    public void uploadAsync(final String mimeType, final java.io.File content, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return upload(mimeType, content);
            }
        });
    }

    @Override
    public String share(String user){
        return getDriven().getSharing().share(this, user);
    }

    @Override
    public String share(String user, int kind) {
        return getDriven().getSharing().share(this, user, kind);
    }

    @Override
    public void shareAsync(final String user, Task<String> result){
        doAsync(result, new Delegate<String>() {
            @Override public String invoke() {
                return share(user);
            }
        });
    }

    @Override
    public void shareAsync(final String user, final int kind, Task<String> result) {
        doAsync(result, new Delegate<String>() {
            @Override public String invoke() {
                return share(user, kind);
            }
        });
    }

    @Override
    public boolean delete(){
        return getDriven().delete(getId());
    }

    @Override
    public void deleteAsync(Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return delete();
            }
        });
    }

    @Override
    public boolean rename(String name) {
        fileModel.setTitle(name);
        return consume(getDriven().update(this, null));
    }

    @Override
    public void renameAsync(final String name, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return rename(name);
            }
        });
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

    private boolean consume(DrivenFile drivenFile){
        if(!(drivenFile instanceof GoogleDriveFile))
            return false;
        GoogleDriveFile other = (GoogleDriveFile) drivenFile;
        return other.fileModel != null && init(other.fileModel, other.hasDetails);
    }
}
