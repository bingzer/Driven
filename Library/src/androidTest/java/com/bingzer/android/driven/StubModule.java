package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.DrivenServiceProvider;
import com.google.api.services.drive.StubServiceProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true, injects = Driven.class )
public class StubModule {

    @Provides
    @Singleton
    DrivenServiceProvider provideServiceProvider(){
        return new StubServiceProvider();
    }
}
