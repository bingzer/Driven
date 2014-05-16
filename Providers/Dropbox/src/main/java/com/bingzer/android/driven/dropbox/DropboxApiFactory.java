package com.bingzer.android.driven.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.Session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DropboxApiFactory {

    <T extends Session> DropboxAPI<T> createApi(T session);

    OutputStream createOutputStream(File file) throws IOException;

    InputStream createInputStream(File file) throws IOException;

    static class Default implements DropboxApiFactory {

        @Override
        public <T extends Session> DropboxAPI<T> createApi(T session) {
            return new DropboxAPI<T>(session);
        }

        @Override
        public OutputStream createOutputStream(File file) throws IOException{
            return new BufferedOutputStream(new FileOutputStream(file));
        }

        @Override
        public InputStream createInputStream(File file) throws IOException{
            return new BufferedInputStream(new FileInputStream(file));
        }
    }
}
