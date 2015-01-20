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
package com.bingzer.android.driven.local.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.local.ExternalDrive;

public class ExternalDriveActivity extends Activity {
    public static final String BUNDLE_KEY_LOGIN = "com.bingzer.android.driven.local.app.login";
    public static final String BUNDLE_KEY_ROOT = "com.bingzer.android.driven.local.app.rootPath";

    public static final int REQUEST_LOGIN = 300;
    public static final int REQUEST_ACCOUNT_PICKER = 1;
    public static final int REQUEST_AUTHORIZATION = 2;

    private static StorageProvider storageProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null){
            String rootPath = getIntent().getStringExtra(BUNDLE_KEY_ROOT);
            if(rootPath != null){
                storageProvider = new ExternalDrive();
                Result<DrivenException> result = storageProvider.authenticate(new Credential(this, rootPath));
                if(result.isSuccess()) {
                    successfullyAuthenticated();
                    return;
                }
                else{
                    throw new DrivenException(result.getException());
                }
            }
        }

        throw new DrivenException("Root path is not specified");
    }

    private void successfullyAuthenticated(){
        setResult(RESULT_OK);
        finish();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void launch(Activity activity, String rootPath){
        launch(activity, rootPath, REQUEST_LOGIN);
    }

    public static void launch(Activity activity, String rootPath, int requestCode){
        activity.startActivityForResult(createLoginIntent(activity, rootPath), requestCode);
    }

    public static Intent createLoginIntent(Activity activity, String rootPath){
        Intent intent = new Intent(activity, ExternalDriveActivity.class);
        intent.putExtra(BUNDLE_KEY_LOGIN, REQUEST_LOGIN);
        intent.putExtra(BUNDLE_KEY_ROOT, rootPath);
        return intent;
    }

}
