package com.bingzer.android.driven.dropbox.app;

import android.app.Activity;
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

    public static final int REQUEST_LOGIN = 3;

    private static Dropbox driven = new Dropbox();
    private DrivenCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent() != null && getIntent().getIntExtra(BUNDLE_KEY_LOGIN, 0) == REQUEST_LOGIN){
            String appKey = getIntent().getStringExtra(BUNDLE_KEY_APPKEY);
            String appSecret = getIntent().getStringExtra(BUNDLE_KEY_APPSECRET);

            DrivenCredential.Token token = new DrivenCredential.Token(appKey, appSecret);
            credential = new DrivenCredential(this, token);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (driven.getDropboxApi().getSession().authenticationSuccessful()) {
                driven.getDropboxApi().getSession().finishAuthentication();
                credential.getToken().setAccessToken(driven.getDropboxApi().getSession().getOAuth2AccessToken());
                requestAuthorization();
            }
        }
        catch (DrivenException e){
            requestAuthorization();
        }
    }

    private void requestAuthorization(){
        driven.authenticateAsync(credential, new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                if(!result.isSuccess()){
                    driven.getDropboxApi().getSession().startOAuth2Authentication(DropboxActivity.this);
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
