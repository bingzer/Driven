package com.bingzer.android.driven.gmsdrive;

import android.app.Activity;
import android.content.Context;

import com.bingzer.android.driven.Credential;

public class GmsCredential extends Credential {

    private Activity callingActivity;

    public GmsCredential(Context context) {
        super(context);

        if (context instanceof Activity){
            callingActivity = (Activity) context;
        }
    }

    public Activity getCallingActivity(){
        return callingActivity;
    }
}
