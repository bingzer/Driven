package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.DrivenFile;

public class Path {
    public static String combine(DrivenFile drivenFile, String title){
        if(drivenFile != null)
            return combine(drivenFile.toString(), title);
        else
            return combine("", title);
    }

    public static String combine(String path1, String path2){
        if(path1 != null)
            return path1 + "/" + path2;
        else
            return "/" + path2;
    }
}
