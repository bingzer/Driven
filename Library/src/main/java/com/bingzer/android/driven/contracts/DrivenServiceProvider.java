package com.bingzer.android.driven.contracts;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;

/**
 * Created by Ricky on 5/7/2014.
 */
public interface DrivenServiceProvider {
    public Drive createGoogleDriveService(GoogleAccountCredential credential);
}
