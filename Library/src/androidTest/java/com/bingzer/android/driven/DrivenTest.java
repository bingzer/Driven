package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.DriveUtils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

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

        assertTrue(driven.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_authenticateAsync() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        driven.authenticateAsync(credential, new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertTrue(driven.isAuthenticated());
                assertTrue(result.isSuccess());
                assertNull(result.getException());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_deauthenticate(){
        driven.authenticate(credential);
        assertTrue(driven.isAuthenticated());

        driven.deauthenticate(getContext());
        assertFalse(driven.isAuthenticated());
    }


    public void test_deauthenticateAsync() throws InterruptedException {
        driven.authenticate(credential);
        assertTrue(driven.isAuthenticated());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.deauthenticateAsync(getContext(), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertFalse(driven.isAuthenticated());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_authenticate_NoSave(){
        Result<DrivenException> result = driven.authenticate(DriveUtils.createGoogleAccountCredential(getContext(), null), false);

        assertTrue(driven.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_getDrivenUser() throws Exception{
        driven.authenticate(credential);

        assertEquals("Name", driven.getDrivenUser().getName());
        assertEquals("DisplayName", driven.getDrivenUser().getDisplayName());
        assertEquals("EmailAddress", driven.getDrivenUser().getEmailAddress());
    }

    public void test_getDrivenService(){
        try {
            assertNull(driven.getDrivenService());
            fail("Should throw exception");
        }
        catch (DrivenException e){
            // -- ignore
        }

        driven.authenticate(credential);
        assertNotNull(driven.getDrivenService());
    }

    public void test_getSharedWithMe(){
        assertNotNull(driven.getShared());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_get_notAuthenticated() {
        try {
            driven.get("01");
            fail("Should throw exception");
        } catch (DrivenException e) {
            // -- ignore
        }
    }

    public void test_get() throws Exception{
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeType01", drivenFile.getType());
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id01", drivenFile.getModel().getId());
        assertEquals("Description01", drivenFile.getModel().getDescription());
        assertEquals("MimeType01", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl01", drivenFile.getModel().getDownloadUrl());
    }

    /*

    public void test_getAsync() throws Exception {
        driven.authenticate(credential);
        Drive mockDrive = driven.getDrivenService();
        when(mockDrive.files().get(anyString()).setFields(anyString()).execute()).thenReturn(createTestFile());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("01", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                testDrivenFile(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        driven.authenticate(credential);
        Drive mockDrive = driven.getDrivenService();
        when(mockDrive.files().list().execute()).thenReturn(createTestFileList());

        DrivenFile drivenFile = driven.title("01");

        testDrivenFile(drivenFile);
    }

    public void test_titleAsync() throws Exception {
        driven.authenticate(credential);
        Drive mockDrive = driven.getDrivenService();
        when(mockDrive.files().list().execute()).thenReturn(createTestFileList());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.titleAsync("01", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                testDrivenFile(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private FileList createTestFileList(){
        ArrayList<File> files = new ArrayList<File>();
        files.add(createTestFile());
        return new FileList().setItems(files);
    }

    private File createTestFile(){
        return new File().setId("Id").setTitle("Title")
                .setMimeType("MimeType").setDownloadUrl("DownloadUrl").setDescription("Description")
                .setEtag("Etag");
    }

    private void testDrivenFile(DrivenFile drivenFile){
        assertNotNull(drivenFile);
        assertEquals("Id", drivenFile.getId());
        assertEquals("Title", drivenFile.getTitle());
        assertEquals("MimeType", drivenFile.getType());
        assertEquals("DownloadUrl", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id", drivenFile.getModel().getId());
        assertEquals("Description", drivenFile.getModel().getDescription());
        assertEquals("MimeType", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl", drivenFile.getModel().getDownloadUrl());
    }
    */

}
