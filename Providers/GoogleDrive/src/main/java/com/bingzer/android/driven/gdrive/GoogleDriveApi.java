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
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.List;

/**
 * Contract for a DrivenService.
 * We're decoupling this so we can unit-test. Nothing more
 */
interface GoogleDriveApi {

    Drive.About about();
    Drive.Files files();
    Drive.Permissions permissions();
    HttpRequestFactory getRequestFactory();

    ///////////////////////////////////////////////////////////////////////////////

    static class Default implements GoogleDriveApi {

        private Drive drive;

        Default(Drive drive){
            this.drive = drive;
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

    ///////////////////////////////////////////////////////////////////////////////

    static interface Factory {

        /**
         * Returns DrivenService
         */
        GoogleDriveApi createApi(DrivenCredential credential);

        static class Default implements Factory {

            @Override
            public GoogleDriveApi createApi(DrivenCredential credential) {
                return new GoogleDriveApi.Default(createGoogleDriveService(credential));
            }

            private Drive createGoogleDriveService(DrivenCredential credential) {
                GoogleAccountCredential account = createGoogleAccountCredential(credential.getContext(), credential.getAccountName());
                return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), account).build();
            }

            private GoogleAccountCredential createGoogleAccountCredential(Context context, String username) {
                List<String> list = new ArrayList<String>();
                list.add(DriveScopes.DRIVE);

                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, list);
                if(username != null)
                    credential.setSelectedAccountName(username);

                return credential;
            }
        }
    }

}
