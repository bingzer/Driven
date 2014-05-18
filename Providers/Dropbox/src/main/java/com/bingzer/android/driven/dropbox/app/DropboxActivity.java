package com.bingzer.android.driven.dropbox.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.dropbox.Dropbox;

public class DropboxActivity extends Activity {
    public static final String BUNDLE_KEY_LOGIN = "com.bingzer.android.driven.dropbox.app.login";
    public static final String BUNDLE_KEY_APPKEY = "com.bingzer.android.driven.dropbox.app.appKey";
    public static final String BUNDLE_KEY_APPSECRET = "com.bingzer.android.driven.dropbox.app.appSecret";

    public static final int REQUEST_LOGIN = 200;

    private static Dropbox driven = new Dropbox();
    private DrivenCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credential = new DrivenCredential(this);
        if(!driven.hasSavedCredentials(this)){
            showAccountChooser();
        }
        else{
            authenticate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try{
            if (driven.getDropboxApi().getSession().authenticationSuccessful()) {
                driven.getDropboxApi().getSession().finishAuthentication();
                credential.getToken().setAccessToken(driven.getDropboxApi().getSession().getOAuth2AccessToken());
            }
            authenticate();
        }
        catch (DrivenException e){
            authenticate();
        }
    }

    private void successfullyAuthorized(){
        setResult(RESULT_OK);
        finish();
    }

    private void showAccountChooser(){
        if(getIntent() != null && getIntent().getIntExtra(BUNDLE_KEY_LOGIN, 0) == REQUEST_LOGIN){
            String appKey = getIntent().getStringExtra(BUNDLE_KEY_APPKEY);
            String appSecret = getIntent().getStringExtra(BUNDLE_KEY_APPSECRET);

            DrivenCredential.Token token = new DrivenCredential.Token(appKey, appSecret);
            credential.setToken(token);
        }
    }

    private void authenticate(){
        driven.authenticateAsync(credential, new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                if(!result.isSuccess()){
                    driven.getDropboxApi().getSession().startOAuth2Authentication(DropboxActivity.this);
                }
                else{
                    // we're done!
                    successfullyAuthorized();
                }
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////


    public static void launch(Activity activity, String appKey, String appSecret){
        launch(activity, REQUEST_LOGIN, appKey, appSecret);
    }

    public static void launch(Activity activity, int requestCode, String appKey, String appSecret){
        activity.startActivityForResult(createLoginIntent(activity, appKey, appSecret), requestCode);
    }

    public static Intent createLoginIntent(Activity activity, String appKey, String appSecret){
        Intent intent = new Intent(activity, DropboxActivity.class);
        intent.putExtra(BUNDLE_KEY_LOGIN, REQUEST_LOGIN);
        intent.putExtra(BUNDLE_KEY_APPKEY, appKey);
        intent.putExtra(BUNDLE_KEY_APPSECRET, appSecret);
        return intent;
    }
}
