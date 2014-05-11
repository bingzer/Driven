package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.DrivenService;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;

public class DrivenServiceProxy implements DrivenService {

    private Drive drive;

    public DrivenServiceProxy(Drive drive){
        this.drive = drive;
    }

    @Override
    public Drive getDrive() {
        return drive;
    }

    @Override
    public Drive.About about() {
        return drive.about();
    }

    @Override
    public Drive.Files files() {
        return drive.files();
    }

    @Override
    public Drive.Permissions permissions() {
        return drive.permissions();
    }

    @Override
    public HttpRequestFactory getRequestFactory() {
        return drive.getRequestFactory();
    }
}
