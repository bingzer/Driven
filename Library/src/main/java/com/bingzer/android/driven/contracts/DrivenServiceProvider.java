package com.bingzer.android.driven.contracts;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by Ricky on 5/7/2014.
 */
public interface DrivenServiceProvider {
    public DrivenService createGoogleDriveService(GoogleAccountCredential credential);
}
