package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.utils.DriveUtils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.StubServiceProvider;

import dagger.ObjectGraph;

public class DrivenTest extends AndroidTestCase{

    private Driven driven;
    private GoogleAccountCredential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(Driven.class);
        credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");
    }

    /**
     * Make sure that we have an instance of
     * GoogleDriveService provider when calling by Driven.getDriven()
     * for public API access
     */
    public void test_DrivenAccessForPublic(){
        Driven driven = Driven.getDriven();
        assertTrue(driven.getServiceProvider() instanceof GoogleDriveServiceProvider);
    }

    public void test_DrivenAccessForTest(){
        assertTrue(driven.getServiceProvider() instanceof StubServiceProvider);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_authenticate(){
        Result<DrivenException> result = driven.authenticate(credential);

        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_authenticate_NoSave(){
        Result<DrivenException> result = driven.authenticate(DriveUtils.createGoogleAccountCredential(getContext(), null), false);

        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_getDrivenUser(){
        driven.authenticate(credential);
        assertEquals("Name", driven.getDrivenUser().getName());
        assertEquals("DisplayName", driven.getDrivenUser().getDisplayName());
        assertEquals("EmailAddress", driven.getDrivenUser().getEmailAddress());
    }


}
