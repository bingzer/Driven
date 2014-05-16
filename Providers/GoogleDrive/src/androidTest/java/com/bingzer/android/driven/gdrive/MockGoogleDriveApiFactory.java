package com.bingzer.android.driven.gdrive;


import com.bingzer.android.driven.DrivenCredential;

public class MockGoogleDriveApiFactory implements GoogleDriveApi.Factory {

    @Override
    public GoogleDriveApi createApi(DrivenCredential credential) {
        return new MockGoogleDriveApi();
    }
}
