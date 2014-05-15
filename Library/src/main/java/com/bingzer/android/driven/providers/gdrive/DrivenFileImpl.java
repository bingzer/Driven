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
package com.bingzer.android.driven.providers.gdrive;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.DrivenProvider;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

/**
 * A wrapper for "File" in GoogleDrive side
 */
@SuppressWarnings("unused")
class DrivenFileImpl implements DrivenFile {
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    protected static DrivenProvider drivenProvider;
    protected static void setDrivenProvider(DrivenProvider drivenProvider){
        DrivenFileImpl.drivenProvider = drivenProvider;
    }

    protected static DrivenProvider getDrivenProvider(){
        if(drivenProvider == null)
            drivenProvider = Driven.getProvider(Driven.GOOGLE_DRIVE);
        return drivenProvider;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    private String id;
    private String title;
    private String type;
    private String downloadUrl;
    private File fileModel;
    private boolean hasDetails;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected DrivenFileImpl(File file, boolean hasDetails){
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
    public String getTitle(){
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
        if(!hasDetails) consume(getDrivenProvider().getDetails(this));
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
    public Iterable<DrivenFile> list() {
        return getDrivenProvider().list(this);
    }

    @Override
    public void listAsync(Task<Iterable<DrivenFile>> result) {
        doAsync(result, new Delegate<Iterable<DrivenFile>>() {
            @Override public Iterable<DrivenFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public java.io.File download(java.io.File local) {
        return getDrivenProvider().download(this, local);
    }

    @Override
    public void downloadAsync(final java.io.File local, Task<java.io.File> result) {
        doAsync(result, new Delegate<java.io.File>() {
            @Override public java.io.File invoke() {
                return download(local);
            }
        });
    }

    @Override
    public boolean upload(String mimeType, java.io.File content) {
        return consume(getDrivenProvider().update(this, new DrivenContentImpl(mimeType, content)));
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
    public boolean share(String user){
        return getDrivenProvider().share(this, user);
    }

    @Override
    public void shareAsync(final String user, Task<Boolean> result){
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return share(user);
            }
        });
    }

    @Override
    public boolean delete(){
        return getDrivenProvider().delete(getId());
    }

    @Override
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
        downloadUrl = fileModel.getDownloadUrl();
        hasDetails = details;
        return true;
    }

    private boolean consume(DrivenFile drivenFile){
        if(!(drivenFile instanceof DrivenFileImpl))
            return false;
        DrivenFileImpl other = (DrivenFileImpl) drivenFile;
        return other.fileModel != null && init(other.fileModel, other.hasDetails);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static Iterable<DrivenFile> from(FileList fileList){
        List<DrivenFile> list = new ArrayList<DrivenFile>();
        for(int i = 0; i < fileList.size(); i++){
            list.add(new DrivenFileImpl(fileList.getItems().get(i), false));
        }

        return list;
    }
}
