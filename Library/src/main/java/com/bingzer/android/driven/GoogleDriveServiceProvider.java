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
package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.DrivenService;
import com.bingzer.android.driven.contracts.DrivenServiceProvider;
import com.bingzer.android.driven.utils.DriveUtils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Default implementation for DrivenServiceProvider
 */
public class GoogleDriveServiceProvider implements DrivenServiceProvider {
    @Override
    public DrivenService createGoogleDriveService(GoogleAccountCredential credential) {
        return new DrivenServiceProxy(DriveUtils.createGoogleDriveService(credential));
    }
}
