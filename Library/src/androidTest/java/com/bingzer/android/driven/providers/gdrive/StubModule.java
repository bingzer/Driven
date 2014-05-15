package com.bingzer.android.driven.providers.gdrive;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("ALL")
@Module(library = true, injects = GoogleDrive.class )
public class StubModule {

    @Provides
    @Singleton
    ProxyCreator provideProxyCreator(){
        return new MockProxyCreator();
    }
}
