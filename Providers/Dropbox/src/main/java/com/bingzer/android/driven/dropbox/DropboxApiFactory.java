package com.bingzer.android.driven.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.Session;

public interface DropboxApiFactory {

    <T extends Session> DropboxAPI<T> createApi(T session);

    static class Default implements DropboxApiFactory {

        @Override
        public <T extends Session> DropboxAPI<T> createApi(T session) {
            return new DropboxAPI<T>(session);
        }
    }
}
