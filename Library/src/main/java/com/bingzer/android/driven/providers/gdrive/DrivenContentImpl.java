package com.bingzer.android.driven.providers.gdrive;

import com.bingzer.android.driven.DrivenContent;

import java.io.File;

class DrivenContentImpl implements DrivenContent {

    private File file;
    private String type;

    DrivenContentImpl(String type, File file){
        this.type = type;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
