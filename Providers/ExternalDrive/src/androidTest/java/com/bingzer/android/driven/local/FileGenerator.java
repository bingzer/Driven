package com.bingzer.android.driven.local;

import com.bingzer.android.driven.utils.IOUtils;

import java.io.File;

public class FileGenerator {

    public static void generate(File root) throws Exception{
        safeCreateDir(root);

        File folder100 = new File(root, "Folder100");
        IOUtils.safeCreateDir(folder100);

        safeCreateFile(new File(folder100, "File101"));
        safeCreateFile(new File(folder100, "File102"));
        safeCreateFile(new File(folder100, "File103"));

        safeCreateFile(new File(root, "File001"));
        safeCreateFile(new File(root, "File002"));
        safeCreateFile(new File(root, "File003"));

    }

    public static void clean(File root) throws Exception {
        IOUtils.deleteTree(root, false);
    }

    //////////////////////////////////////////////

    static void safeCreateDir(File dir){
        IOUtils.safeCreateDir(dir);
    }

    static void safeCreateFile(File file) throws Exception{
        if(!file.exists()){
            file.createNewFile();
        }
    }

}
