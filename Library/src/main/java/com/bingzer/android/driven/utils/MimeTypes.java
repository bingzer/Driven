package com.bingzer.android.driven.utils;

import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Utility class to easily get mime types
 */
public final class MimeTypes {

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url){
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(File file){
        return getMimeType(file.getName());
    }
}
