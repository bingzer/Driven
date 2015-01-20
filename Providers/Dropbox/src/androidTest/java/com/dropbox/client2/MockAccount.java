package com.dropbox.client2;

public class MockAccount extends DropboxAPI.Account {
    public MockAccount(String country, String displayName, long uid, String referralLink, long quota, long quotaNormal, long quotaShared) {
        super(country, displayName, uid, referralLink, quota, quotaNormal, quotaShared);
    }
}
