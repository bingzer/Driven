package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.DrivenService;
import com.bingzer.android.driven.contracts.DrivenServiceProvider;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class StubServiceProvider implements DrivenServiceProvider {

    @Override
    public DrivenService createService(GoogleAccountCredential credential) {
        return new MockDrivenService();
    }

}
