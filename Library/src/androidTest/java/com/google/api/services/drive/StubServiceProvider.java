package com.google.api.services.drive;

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
    public Drive createGoogleDriveService(GoogleAccountCredential credential) {
        return mockDrive;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private Drive mockDrive;

    public StubServiceProvider() {
        mockDrive = mock(Drive.class, RETURNS_DEEP_STUBS);
        try {
            when(mockDrive.about().get().setFields("name,user").execute())
                    .thenReturn(new About().setName("Name").setUser(new User().setDisplayName("DisplayName").setEmailAddress("EmailAddress")));
        } catch (IOException e) {
            fail("Exception: " + e);
        }
    }

}
