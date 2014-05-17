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
package com.bingzer.android.driven.gdrive.app;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.gdrive.GoogleDrive;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.List;

public class GoogleDriveActivity extends Activity {
    public static final String BUNDLE_KEY_LOGIN = "com.bingzer.android.driven.gdrive.app.login";

    public static final int REQUEST_LOGIN = 100;
    public static final int REQUEST_ACCOUNT_PICKER = 1;
    public static final int REQUEST_AUTHORIZATION = 2;

    private static Driven driven = new GoogleDrive();
    private GoogleAccountCredential googleAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleAccount = createGoogleAccountCredential(this);
        if (!driven.hasSavedCredentials(this))
            showAccountChooser();
        else
            authenticate();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
                    requestAuthorization(data);
                else
                    finish();
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    requestAuthorization(data);
                }
                else {
                    startActivityForResult(googleAccount.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }

                break;
        }
    }

    private void requestAuthorization(Intent data){
        final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            googleAccount.setSelectedAccountName(accountName);
            authenticate();
        }
    }

    private void successfullyAuthenticated(){
        setResult(RESULT_OK);
        finish();
    }

    private void showAccountChooser(){
        if(getIntent() != null && getIntent().getIntExtra(BUNDLE_KEY_LOGIN, 0) == REQUEST_LOGIN){
            startActivityForResult(googleAccount.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
    }

    private void authenticate(){
        driven.authenticateAsync(new DrivenCredential(this, googleAccount.getSelectedAccountName()), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                if(result.isSuccess())
                    successfullyAuthenticated();
                else{
                    if(result.getException().getCause() instanceof UserRecoverableAuthIOException){
                        UserRecoverableAuthIOException exception = (UserRecoverableAuthIOException) result.getException().getCause();
                        startActivityForResult(exception.getIntent(), REQUEST_AUTHORIZATION);
                    }
                    else{
                        throw result.getException();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static GoogleAccountCredential createGoogleAccountCredential(Context context) {
        List<String> list = new ArrayList<String>();
        list.add(DriveScopes.DRIVE);

        return GoogleAccountCredential.usingOAuth2(context, list);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void launch(Activity activity){
        launch(activity, REQUEST_LOGIN);
    }

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(createLoginIntent(activity), requestCode);
    }

    public static Intent createLoginIntent(Activity activity){
        Intent intent = new Intent(activity, GoogleDriveActivity.class);
        intent.putExtra(BUNDLE_KEY_LOGIN, REQUEST_LOGIN);
        return intent;
    }

}
