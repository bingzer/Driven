package com.bingzer.android.driven.providers.gdrive;


import com.bingzer.android.driven.DrivenCredential;

public class MockProxyCreator implements ProxyCreator {

    @Override
    public Proxy createProxy(DrivenCredential credential) {
        return new MockProxy();
    }
}
