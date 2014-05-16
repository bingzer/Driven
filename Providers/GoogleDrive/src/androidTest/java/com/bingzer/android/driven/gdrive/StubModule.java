package com.bingzer.android.driven.gdrive;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("ALL")
@Module(library = true, injects = GoogleDrive.class )
public class StubModule {

    @Provides
    @Singleton
    GoogleDriveApi.Factory provideProxyCreator(){
        return new MockGoogleDriveApiFactory();
    }
}
