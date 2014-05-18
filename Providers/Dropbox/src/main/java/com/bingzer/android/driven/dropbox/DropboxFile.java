package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.api.Path;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.util.List;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

class DropboxFile implements DrivenFile {

    protected static Driven driven;
    protected static void setDriven(Driven driven){
        DropboxFile.driven = driven;
    }
    protected static Driven getDriven(){
        if(driven == null)
            driven = new Dropbox();
        return driven;
    }

    private String id;
    private String title;
    private String downloadUrl;
    private String type;
    private boolean isDirectory;
    private DropboxAPI.Entry model;

    DropboxFile(DropboxAPI.Entry model){
        init(model);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    public String getParentDirectory(){
        return Path.getDirectory(id);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean fetchDetails() {
        return hasDetails();
    }

    @Override
    public void fetchDetailsAsync(Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
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
            @Override
            public List<DrivenFile> invoke() {
                return list();
            }
        });
    }

    @Override
    public DrivenContent download(File local) {
        return getDriven().download(this, local);
    }

    @Override
    public void downloadAsync(final File local, Task<DrivenContent> result) {
        doAsync(result, new Delegate<DrivenContent>() {
            @Override
            public DrivenContent invoke() {
                return download(local);
            }
        });
    }

    @Override
    public boolean upload(String mimeType, File content) {
        return consume(getDriven().update(this, new DrivenContent(mimeType, content)));
    }

    @Override
    public void uploadAsync(final String mimeType, final File content, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return upload(mimeType, content);
            }
        });
    }

    @Override
    public String share(String user) {
        return getDriven().getSharing().share(this, user);
    }

    @Override
    public String share(String user, int kind) {
        return getDriven().getSharing().share(this, user, kind);
    }

    @Override
    public void shareAsync(final String user, Task<String> result) {
        doAsync(result, new Delegate<String>() {
            @Override
            public String invoke() {
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
    public boolean delete() {
        return getDriven().delete(id);
    }

    @Override
    public void deleteAsync(Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override
            public Boolean invoke() {
                return delete();
            }
        });
    }

    @Override
    public boolean rename(String name) {
        try {
            String newPath = Path.combine(getParentDirectory(), name);
            DropboxAPI.Entry entry = ((Dropbox) getDriven()).getDropboxApi().move(id, newPath);
            return init(entry);
        }
        catch (DropboxException e){
            return false;
        }
    }

    @Override
    public void renameAsync(final String name, Task<Boolean> result) {
        doAsync(result, new Delegate<Boolean>() {
            @Override public Boolean invoke() {
                return rename(name);
            }
        });
    }

    @Override
    public String toString() {
        return getId();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean init(DropboxAPI.Entry entry){
        this.model = entry;
        id = entry.path;
        type = entry.mimeType;
        isDirectory = entry.isDir;
        title = entry.fileName();
        downloadUrl = entry.path;
        return true;
    }

    private boolean consume(DrivenFile drivenFile){
        if(!(drivenFile instanceof DropboxFile))
            return false;
        DropboxFile other = (DropboxFile) drivenFile;
        return init(other.model);
    }
}
