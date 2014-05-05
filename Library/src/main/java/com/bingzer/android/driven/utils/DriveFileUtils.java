package com.bingzer.android.driven.utils;

import com.bingzer.android.driven.DriveFile;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricky on 5/5/2014.
 */
public final class DriveFileUtils {

    public static Iterable<DriveFile> toIterables(FileList fileList){
        List<DriveFile> list = new ArrayList<DriveFile>();
        for(int i = 0; i < fileList.size(); i++){
            list.add(new DriveFile(fileList.getItems().get(i)));
        }

        return list;
    }

    //----------------------------------------------------------------------
    private DriveFileUtils(){
        // nothing
    }
}
