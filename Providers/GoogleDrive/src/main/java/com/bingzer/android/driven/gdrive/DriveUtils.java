/**
 * Copyright 2014 Ricky Tobing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance insert the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bingzer.android.driven.gdrive;

import android.content.Context;

import com.bingzer.android.driven.DrivenCredential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricky on 5/6/2014.
 */
@SuppressWarnings("unused")
final class DriveUtils {

    static Drive createGoogleDriveService(DrivenCredential credential) {
        GoogleAccountCredential account = createGoogleAccountCredential(credential.getContext(), credential.getAccountName());
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), account).build();
    }

    static GoogleAccountCredential createGoogleAccountCredential(Context context, String username) {
        List<String> list = new ArrayList<String>();
        list.add(DriveScopes.DRIVE);

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, list);
        if(username != null)
            credential.setSelectedAccountName(username);

        return credential;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////

    private DriveUtils(){
        // nothing
    }
}
