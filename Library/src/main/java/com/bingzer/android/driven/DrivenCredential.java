package com.bingzer.android.driven;

import android.content.Context;

public interface DrivenCredential {
    Context getContext();

    String getAccountName();

    void setAccountName(String accountName);
}
