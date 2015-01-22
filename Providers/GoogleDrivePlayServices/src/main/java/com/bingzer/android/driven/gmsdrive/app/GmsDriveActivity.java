package com.bingzer.android.driven.gmsdrive.app;

import android.app.Activity;
import android.content.Intent;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.gmsdrive.GmsCredential;
import com.bingzer.android.driven.gmsdrive.GmsDrive;

/**
 * The Activity
 */
public class GmsDriveActivity extends Activity {

    public static final String BUNDLE_KEY_LOGIN = "com.bingzer.android.driven.gmsdrive.app.login";
    public static final int REQUEST_LOGIN = 400;

    private static final GmsDrive gmsDrive = new GmsDrive();

    @Override
    protected void onResume() {
        super.onResume();
        authenticate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GmsDrive.RC_SIGN_IN){
            // re-auth
            authenticate();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Credential getCredential(){
        Credential credential;
        if(gmsDrive.hasSavedCredential(this))
            credential = gmsDrive.getSavedCredential(this);
        else {
            credential = new GmsCredential(this);
        }
        return credential;
    }

    private void authenticate(){
        gmsDrive.authenticateAsync(getCredential(), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                if (result.isSuccess()) {
                    successfullyAuthenticated();
                }
            }
        });
    }

    private void successfullyAuthenticated(){
        setResult(RESULT_OK);
        finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void launch(Activity activity){
        launch(activity, REQUEST_LOGIN);
    }

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(createLoginIntent(activity), requestCode);
    }

    public static Intent createLoginIntent(Activity activity){
        Intent intent = new Intent(activity, GmsDriveActivity.class);
        intent.putExtra(BUNDLE_KEY_LOGIN, REQUEST_LOGIN);
        return intent;
    }

}
