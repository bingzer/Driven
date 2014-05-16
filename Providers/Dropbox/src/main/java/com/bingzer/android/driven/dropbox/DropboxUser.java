package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.DrivenUser;
import com.dropbox.client2.DropboxAPI;

class DropboxUser implements DrivenUser {

    private String name;
    private String displayName;
    private String emailAddress;

    DropboxUser(DropboxAPI.Account account){
        name = account.displayName;
        displayName = account.displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public String toString() {
        return "DropboxUser{" +
                "emailAddress='" + emailAddress + '\'' +
                ", displayName='" + displayName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
