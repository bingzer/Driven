package com.bingzer.android.driven.gdrive;


import com.bingzer.android.driven.Credential;

public class MockGoogleDriveApiFactory implements GoogleDriveApi.Factory {

    @Override
    public GoogleDriveApi createApi(Credential credential) {
        return new MockGoogleDriveApi();
    }
}
