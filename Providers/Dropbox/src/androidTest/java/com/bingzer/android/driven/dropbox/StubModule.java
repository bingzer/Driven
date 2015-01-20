package com.bingzer.android.driven.dropbox;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("ALL")
@Module(library = true, injects = Dropbox.class )
public class StubModule {

    @Provides
    @Singleton
    DropboxApiFactory provideProxyCreator(){
        return new MockDropboxApiFactory();
    }
}
