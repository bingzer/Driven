package com.bingzer.android.driven.gdrive;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class GoogleDriveTest extends AndroidTestCase{

    private GoogleDrive drivenProvider;
    private DrivenCredential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        drivenProvider = ObjectGraph.create(StubModule.class).get(GoogleDrive.class);
        //credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");
        credential = new DrivenCredential(getContext(), "Test-User");
    }

    public void test_getProxyCreator(){
        assertTrue(drivenProvider.getProxyCreator() instanceof MockProxyCreator);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_authenticate(){
        Result<DrivenException> result = drivenProvider.authenticate(credential);

        assertTrue(drivenProvider.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_authenticateAsync() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.authenticateAsync(credential, new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertTrue(drivenProvider.isAuthenticated());
                assertTrue(result.isSuccess());
                assertNull(result.getException());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_deauthenticate(){
        drivenProvider.authenticate(credential);
        assertTrue(drivenProvider.isAuthenticated());

        drivenProvider.deauthenticate(getContext());
        assertFalse(drivenProvider.isAuthenticated());
    }


    public void test_deauthenticateAsync() throws InterruptedException {
        drivenProvider.authenticate(credential);
        assertTrue(drivenProvider.isAuthenticated());

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.deauthenticateAsync(getContext(), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertFalse(drivenProvider.isAuthenticated());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_authenticate_NoSave(){
        Result<DrivenException> result = drivenProvider.authenticate(credential, false);

        assertTrue(drivenProvider.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_getDrivenUser() throws Exception{
        drivenProvider.authenticate(credential);

        assertEquals("Name", drivenProvider.getDrivenUser().getName());
        assertEquals("DisplayName", drivenProvider.getDrivenUser().getDisplayName());
        assertEquals("EmailAddress", drivenProvider.getDrivenUser().getEmailAddress());
    }

    public void test_getDrivenService(){
        try {
            assertNull(drivenProvider.getProxy());
            fail("Should throw exception");
        }
        catch (DrivenException e){
            // -- ignore
        }

        drivenProvider.authenticate(credential);
        assertNotNull(drivenProvider.getProxy());
    }

    public void test_getSharedWithMe(){
        assertNotNull(drivenProvider.getShared());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_async_fail() throws Exception {
        // we don't authenticate now it should throw error
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.getAsync("Title01", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                fail("Should throw error");
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_get_notAuthenticated() {
        try {
            drivenProvider.id("Id01");
            fail("Should throw exception");
        } catch (DrivenException e) {
            // -- ignore
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_exists() throws Exception {
        drivenProvider.authenticate(credential);

        assertTrue(drivenProvider.exists("Title01"));
        assertTrue(drivenProvider.exists("Title02"));
        assertTrue(drivenProvider.exists("Title03"));

        assertFalse(drivenProvider.exists("Title89"));
        assertFalse(drivenProvider.exists("Title99"));
    }

    public void test_existsAsync() throws Exception {
        drivenProvider.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.existsAsync("Title01", new Task<Boolean>() {
            @Override public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();

        final CountDownLatch signal2 = new CountDownLatch(1);
        drivenProvider.existsAsync("Title101", new Task<Boolean>() {
            @Override public void onCompleted(Boolean result) {
                assertFalse(result);
                signal2.countDown();
            }
        });
        signal2.await();
    }

    public void test_exists_inParent() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        drivenProvider.create(parent, "File101");

        assertTrue(drivenProvider.exists(parent, "File101"));
    }

    public void test_existsAsync_inParent() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        drivenProvider.create(parent, "File102");

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.existsAsync(parent, "File102", new Task<Boolean>() {
            @Override public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_get() throws Exception{
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.id("Id01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeType01", drivenFile.getType());
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id01", ((GoogleDriveFile)drivenFile).getModel().getId());
        assertEquals("Description01", ((GoogleDriveFile)drivenFile).getModel().getDescription());
        assertEquals("MimeType01", ((GoogleDriveFile)drivenFile).getModel().getMimeType());
        assertEquals("DownloadUrl01", ((GoogleDriveFile)drivenFile).getModel().getDownloadUrl());
    }

    public void test_getAsync() throws Exception {
        drivenProvider.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.idAsync("Id03", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile drivenFile) {
                assertNotNull(drivenFile);
                assertEquals("Id03", drivenFile.getId());
                assertEquals("Title03", drivenFile.getTitle());
                assertEquals("MimeType03", drivenFile.getType());
                assertEquals("DownloadUrl03", drivenFile.getDownloadUrl());
                assertFalse(drivenFile.hasDetails());

                // check raw model
                assertEquals("Id03", ((GoogleDriveFile)drivenFile).getModel().getId());
                assertEquals("Description03", ((GoogleDriveFile)drivenFile).getModel().getDescription());
                assertEquals("MimeType03", ((GoogleDriveFile)drivenFile).getModel().getMimeType());
                assertEquals("DownloadUrl03", ((GoogleDriveFile)drivenFile).getModel().getDownloadUrl());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.get("Title02");

        assertNotNull(drivenFile);
        assertEquals("Id02", drivenFile.getId());
        assertEquals("Title02", drivenFile.getTitle());
        assertEquals("MimeType02", drivenFile.getType());
        assertEquals("DownloadUrl02", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id02", ((GoogleDriveFile)drivenFile).getModel().getId());
        assertEquals("Description02", ((GoogleDriveFile)drivenFile).getModel().getDescription());
        assertEquals("MimeType02", ((GoogleDriveFile)drivenFile).getModel().getMimeType());
        assertEquals("DownloadUrl02", ((GoogleDriveFile)drivenFile).getModel().getDownloadUrl());
    }

    public void test_titleAsync() throws Exception {
        drivenProvider.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.getAsync("Title01", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("Id01", result.getId());
                assertEquals("Title01", result.getTitle());
                assertEquals("MimeType01", result.getType());
                assertEquals("DownloadUrl01", result.getDownloadUrl());
                assertFalse(result.hasDetails());

                // check raw model
                assertEquals("Id01", ((GoogleDriveFile)result).getModel().getId());
                assertEquals("Description01", ((GoogleDriveFile)result).getModel().getDescription());
                assertEquals("MimeType01", ((GoogleDriveFile)result).getModel().getMimeType());
                assertEquals("DownloadUrl01", ((GoogleDriveFile)result).getModel().getDownloadUrl());
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_update() throws Exception {
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.id("Id01");
        assertNotNull(drivenFile);

        drivenProvider.update(drivenFile, new DrivenContent("MimeTypeEdited01", new File("")));
        drivenFile = drivenProvider.id("Id01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeTypeEdited01", drivenFile.getType());  // we changed this (was MimeType01)
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id01", ((GoogleDriveFile)drivenFile).getModel().getId());
        assertEquals("Description01", ((GoogleDriveFile)drivenFile).getModel().getDescription());
        assertEquals("MimeTypeEdited01", ((GoogleDriveFile)drivenFile).getModel().getMimeType());
        assertEquals("DownloadUrl01", ((GoogleDriveFile)drivenFile).getModel().getDownloadUrl());
    }

    public void test_updateAsync() throws Exception{
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.id("Id03");
        assertNotNull(drivenFile);

        final DrivenContent fileContent = new DrivenContent("MimeTypeEdited03", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.updateAsync(drivenFile, fileContent, new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("Id03", result.getId());
                assertEquals("Title03", result.getTitle());
                assertEquals("MimeTypeEdited03", result.getType());  // we changed this (was MimeType03)
                assertEquals("DownloadUrl03", result.getDownloadUrl());
                assertFalse(result.hasDetails());

                // check raw model
                assertEquals("Id03", ((GoogleDriveFile)result).getModel().getId());
                assertEquals("Description03", ((GoogleDriveFile)result).getModel().getDescription());
                assertEquals("MimeTypeEdited03", ((GoogleDriveFile)result).getModel().getMimeType());
                assertEquals("DownloadUrl03", ((GoogleDriveFile)result).getModel().getDownloadUrl());
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_delete() throws Exception {
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.id("Id03");
        assertNotNull(drivenFile);

        assertTrue(drivenProvider.delete("Id03"));
    }

    public void test_deleteAsync() throws Exception {
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.id("Id02");
        assertNotNull(drivenFile);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.deleteAsync("Id02", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_first() throws Exception {
        drivenProvider.authenticate(credential);
        DrivenFile drivenFile = drivenProvider.first("title = 'Title01'");
        assertNotNull(drivenFile);

        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());
    }

    public void test_firstAsync() throws Exception {
        drivenProvider.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.firstAsync("title = 'Title01'", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);

                assertEquals("Id01", result.getId());
                assertEquals("Title01", result.getTitle());
                assertEquals("MimeType01", result.getType());  // we changed this (was MimeType03)
                assertEquals("DownloadUrl01", result.getDownloadUrl());
                assertFalse(result.hasDetails());
                signal.countDown();
            }
        });
        signal.await();
    }


    public void test_query() throws Exception {
        drivenProvider.authenticate(credential);
        Iterable<DrivenFile> drivenFiles = drivenProvider.query("title = 'Title01'");
        for(DrivenFile drivenFile : drivenFiles){
            assertNotNull(drivenFile);

            assertEquals("Id01", drivenFile.getId());
            assertEquals("Title01", drivenFile.getTitle());
            assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
            assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
            assertFalse(drivenFile.hasDetails());
        }
    }

    public void test_queryAsync() throws Exception {
        drivenProvider.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.queryAsync("title = 'Title01'", new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                for(DrivenFile drivenFile : result){
                    assertNotNull(drivenFile);

                    assertEquals("Id01", drivenFile.getId());
                    assertEquals("Title01", drivenFile.getTitle());
                    assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
                    assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
                    assertFalse(drivenFile.hasDetails());
                }
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_create() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile drivenFile = drivenProvider.create("Folder100");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());

        drivenFile = drivenProvider.id("Folder100");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());
    }

    public void test_createAsync() throws Exception {
        drivenProvider.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.createAsync("Folder100", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                result = drivenProvider.id("Folder100");
                assertNotNull(result);
                assertTrue(result.isDirectory());

                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_create_file() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        DrivenFile drivenFile = drivenProvider.create("File101", fileContent);
        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
        assertEquals("MimeType101", drivenFile.getType());

        drivenFile = drivenProvider.id("File101");
        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
        assertEquals("MimeType101", drivenFile.getType());
    }

    public void test_createAsync_file() throws Exception {
        drivenProvider.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        drivenProvider.createAsync("File101", fileContent, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertFalse(result.isDirectory());
                assertEquals("MimeType101", result.getType());

                result = drivenProvider.id("File101");
                assertNotNull(result);
                assertFalse(result.isDirectory());
                assertEquals("MimeType101", result.getType());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_create_inParent() throws Exception {
        // we're going to create a folder within a parent
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        assertNotNull(parent);

        DrivenFile drivenFile = drivenProvider.create(parent, "Folder110");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());

        // check parent
        // since we're mocking the fields requested are ignored
        // therefore the parent references should be populated
        // without having to have to call drivenFile.getDetails()
        assertEquals("Folder100", ((GoogleDriveFile)drivenFile).getModel().getParents().get(0).getId());
    }

    public void test_createAsync_inParent() throws Exception {
        // we're going to create a folder within a parent
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        assertNotNull(parent);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.createAsync(parent, "Folder110", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                // check parent
                // since we're mocking the fields requested are ignored
                // therefore the parent references should be populated
                // without having to have to call drivenFile.getDetails()
                assertEquals("Folder100", ((GoogleDriveFile)result).getModel().getParents().get(0).getId());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_create_fileInParent() throws Exception {
        // we're going to create a file within a parent
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        assertNotNull(parent);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        DrivenFile drivenFile = drivenProvider.create(parent, "File101", fileContent);

        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
        assertEquals(drivenFile.getType(), "MimeType101");
        assertEquals("Folder100", ((GoogleDriveFile)drivenFile).getModel().getParents().get(0).getId());
    }

    public void test_createAsync_fileInParent() throws Exception {
        // we're going to create a file within a parent
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        assertNotNull(parent);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.createAsync(parent, "File101", fileContent, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertFalse(result.isDirectory());
                assertEquals(result.getType(), "MimeType101");
                assertEquals("Folder100", ((GoogleDriveFile)result).getModel().getParents().get(0).getId());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_list() throws Exception {
        drivenProvider.authenticate(credential);

        int counter = 1;
        for(DrivenFile drivenFile : drivenProvider.list()){
            assertEquals("Id0" + counter, drivenFile.getId());
            assertEquals("Title0" + counter, drivenFile.getTitle());
            assertEquals("MimeType0" + counter, drivenFile.getType());
            assertEquals("DownloadUrl0" + counter, drivenFile.getDownloadUrl());

            counter++;
        }
    }

    public void test_listAsync() throws Exception {
        drivenProvider.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.listAsync(new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                int counter = 1;
                for(DrivenFile drivenFile : result){
                    assertEquals("Id0" + counter, drivenFile.getId());
                    assertEquals("Title0" + counter, drivenFile.getTitle());
                    assertEquals("MimeType0" + counter, drivenFile.getType());
                    assertEquals("DownloadUrl0" + counter, drivenFile.getDownloadUrl());

                    counter++;

                    signal.countDown();
                }
            }
        });

        signal.await();
    }

    public void test_list_Parent() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        drivenProvider.create(parent, "Folder110");
        drivenProvider.create(parent, "Folder120");
        drivenProvider.create(parent, "Folder130");

        int counter = 1;
        for(DrivenFile drivenFile : drivenProvider.list(parent)){
            assertEquals("Folder1" + counter + "0", drivenFile.getTitle());
        }
    }

    public void test_listAsync_Parent() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile parent = drivenProvider.create("Folder100");
        drivenProvider.create(parent, "Folder110");
        drivenProvider.create(parent, "Folder120");
        drivenProvider.create(parent, "Folder130");

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.listAsync(parent, new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                int counter = 1;
                for(DrivenFile drivenFile : result){
                    assertEquals("Folder1" + counter + "0", drivenFile.getTitle());
                }

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_getDetails() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile drivenFile = drivenProvider.id("Id01");
        assertFalse(drivenFile.hasDetails());

        drivenFile = drivenProvider.getDetails(drivenFile);
        assertNotNull(drivenFile);
        assertTrue(drivenFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile drivenFile = drivenProvider.id("Id01");
        assertFalse(drivenFile.hasDetails());

        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.getDetailsAsync(drivenFile, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.hasDetails());

                signal.countDown();
            }
        });

        signal.await();
    }

    /*
    public void test_download() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.get("Id01");
        java.io.File file = driven.download(drivenFile, new java.io.File("local"));
        assertNotNull(file);
    }
    */

    public void test_share() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile drivenFile = drivenProvider.id("Id01");
        assertTrue(drivenProvider.share(drivenFile, "other-user"));
    }

    public void test_shareAsync() throws Exception {
        drivenProvider.authenticate(credential);

        DrivenFile drivenFile = drivenProvider.id("Id01");
        final CountDownLatch signal = new CountDownLatch(1);
        drivenProvider.shareAsync(drivenFile, "other-user", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });

        signal.await();
    }

}
