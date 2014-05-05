package com.bingzer.android.driven.app;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.List;

public class DrivenActivity extends Activity {
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final int REQUEST_AUTHORIZATION = 2;

    private static Driven driven = Driven.getDriven();
    private GoogleAccountCredential credential;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        credential = createGoogleAccountCredential(this);
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
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
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }

                break;
        }
    }

    private void requestAuthorization(Intent data){
        final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            credential.setSelectedAccountName(accountName);
            try {
                driven.authenticate(credential);
            }
            catch (DrivenException e){
                if(e.getCause() instanceof UserRecoverableAuthIOException){
                    UserRecoverableAuthIOException exception = (UserRecoverableAuthIOException) e.getCause();
                    startActivityForResult(exception.getIntent(), REQUEST_AUTHORIZATION);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static GoogleAccountCredential createGoogleAccountCredential(Context context) {
        List<String> list = new ArrayList<String>();
        list.add(DriveScopes.DRIVE);

        return GoogleAccountCredential.usingOAuth2(context, list);
    }


}
