package com.bingzer.android.driven;

import com.bingzer.android.driven.MockDrivenService;
import com.bingzer.android.driven.contracts.DrivenService;
import com.bingzer.android.driven.contracts.DrivenServiceProvider;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.User;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StubServiceProvider implements DrivenServiceProvider {

    @Override
    public DrivenService createGoogleDriveService(GoogleAccountCredential credential) {
        return new MockDrivenService();
    }

}
