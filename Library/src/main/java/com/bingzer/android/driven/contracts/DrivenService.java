package com.bingzer.android.driven.contracts;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;

/**
 * Created by Ricky on 5/10/2014.
 */
public interface DrivenService {
    Drive getDrive();
    Drive.About about();
    Drive.Files files();
    Drive.Permissions permissions();
    HttpRequestFactory getRequestFactory();
}
