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

    private GoogleDrive driven;
    private DrivenCredential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(GoogleDrive.class);
        //credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");
        credential = new DrivenCredential(getContext(), "Test-User");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        driven.clearAuthentication(getContext());
    }

    public void test_getProxyCreator(){
        driven.authenticate(credential);
        assertTrue(driven.getGoogleDriveApi() instanceof MockGoogleDriveApi);
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

        driven.clearAuthentication(getContext());
        assertFalse(driven.isAuthenticated());
    }


    public void test_deauthenticateAsync() throws InterruptedException {
        driven.authenticate(credential);
        assertTrue(driven.isAuthenticated());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.clearAuthenticationAsync(getContext(), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertFalse(driven.isAuthenticated());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_authenticate_NoSave(){
        Result<DrivenException> result = driven.authenticate(credential, false);

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
            assertNull(driven.getGoogleDriveApi());
            fail("Should throw exception");
        }
        catch (DrivenException e){
            // -- ignore
        }

        driven.authenticate(credential);
        assertNotNull(driven.getGoogleDriveApi());
    }

    public void test_getSharedWithMe(){
        assertNotNull(driven.getShared());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_async_fail() throws Exception {
        // we don't authenticate now it should throw error
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("Title01", new Task.WithErrorReporting<DrivenFile>() {
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
            driven.id("Id01");
            fail("Should throw exception");
        } catch (DrivenException e) {
            // -- ignore
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_exists() throws Exception {
        driven.authenticate(credential);

        assertTrue(driven.exists("Title01"));
        assertTrue(driven.exists("Title02"));
        assertTrue(driven.exists("Title03"));

        assertFalse(driven.exists("Title89"));
        assertFalse(driven.exists("Title99"));
    }

    public void test_existsAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.existsAsync("Title01", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();

        final CountDownLatch signal2 = new CountDownLatch(1);
        driven.existsAsync("Title101", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertFalse(result);
                signal2.countDown();
            }
        });
        signal2.await();
    }

    public void test_exists_inParent() throws Exception {
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        driven.create(parent, "File101");

        assertTrue(driven.exists(parent, "File101"));
    }

    public void test_existsAsync_inParent() throws Exception {
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        driven.create(parent, "File102");

        final CountDownLatch signal = new CountDownLatch(1);
        driven.existsAsync(parent, "File102", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_get() throws Exception{
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("Id01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getName());
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
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.idAsync("Id03", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile drivenFile) {
                assertNotNull(drivenFile);
                assertEquals("Id03", drivenFile.getId());
                assertEquals("Title03", drivenFile.getName());
                assertEquals("MimeType03", drivenFile.getType());
                assertEquals("DownloadUrl03", drivenFile.getDownloadUrl());
                assertFalse(drivenFile.hasDetails());

                // check raw model
                assertEquals("Id03", ((GoogleDriveFile) drivenFile).getModel().getId());
                assertEquals("Description03", ((GoogleDriveFile) drivenFile).getModel().getDescription());
                assertEquals("MimeType03", ((GoogleDriveFile) drivenFile).getModel().getMimeType());
                assertEquals("DownloadUrl03", ((GoogleDriveFile) drivenFile).getModel().getDownloadUrl());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("Title02");

        assertNotNull(drivenFile);
        assertEquals("Id02", drivenFile.getId());
        assertEquals("Title02", drivenFile.getName());
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
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("Title01", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("Id01", result.getId());
                assertEquals("Title01", result.getName());
                assertEquals("MimeType01", result.getType());
                assertEquals("DownloadUrl01", result.getDownloadUrl());
                assertFalse(result.hasDetails());

                // check raw model
                assertEquals("Id01", ((GoogleDriveFile) result).getModel().getId());
                assertEquals("Description01", ((GoogleDriveFile) result).getModel().getDescription());
                assertEquals("MimeType01", ((GoogleDriveFile) result).getModel().getMimeType());
                assertEquals("DownloadUrl01", ((GoogleDriveFile) result).getModel().getDownloadUrl());
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
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("Id01");
        assertNotNull(drivenFile);

        driven.update(drivenFile, new DrivenContent("MimeTypeEdited01", new File("")));
        drivenFile = driven.id("Id01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getName());
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
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("Id03");
        assertNotNull(drivenFile);

        final DrivenContent fileContent = new DrivenContent("MimeTypeEdited03", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.updateAsync(drivenFile, fileContent, new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("Id03", result.getId());
                assertEquals("Title03", result.getName());
                assertEquals("MimeTypeEdited03", result.getType());  // we changed this (was MimeType03)
                assertEquals("DownloadUrl03", result.getDownloadUrl());
                assertFalse(result.hasDetails());

                // check raw model
                assertEquals("Id03", ((GoogleDriveFile) result).getModel().getId());
                assertEquals("Description03", ((GoogleDriveFile) result).getModel().getDescription());
                assertEquals("MimeTypeEdited03", ((GoogleDriveFile) result).getModel().getMimeType());
                assertEquals("DownloadUrl03", ((GoogleDriveFile) result).getModel().getDownloadUrl());
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
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("Id03");
        assertNotNull(drivenFile);

        assertTrue(driven.delete("Id03"));
    }

    public void test_deleteAsync() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("Id02");
        assertNotNull(drivenFile);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.deleteAsync("Id02", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_first() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.first("title = 'Title01'");
        assertNotNull(drivenFile);

        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getName());
        assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());
    }

    public void test_firstAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.firstAsync("title = 'Title01'", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);

                assertEquals("Id01", result.getId());
                assertEquals("Title01", result.getName());
                assertEquals("MimeType01", result.getType());  // we changed this (was MimeType03)
                assertEquals("DownloadUrl01", result.getDownloadUrl());
                assertFalse(result.hasDetails());
                signal.countDown();
            }
        });
        signal.await();
    }


    public void test_query() throws Exception {
        driven.authenticate(credential);
        Iterable<DrivenFile> drivenFiles = driven.query("title = 'Title01'");
        for(DrivenFile drivenFile : drivenFiles){
            assertNotNull(drivenFile);

            assertEquals("Id01", drivenFile.getId());
            assertEquals("Title01", drivenFile.getName());
            assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
            assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
            assertFalse(drivenFile.hasDetails());
        }
    }

    public void test_queryAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.queryAsync("title = 'Title01'", new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                for (DrivenFile drivenFile : result) {
                    assertNotNull(drivenFile);

                    assertEquals("Id01", drivenFile.getId());
                    assertEquals("Title01", drivenFile.getName());
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
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.create("Folder100");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());

        drivenFile = driven.id("Folder100");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());
    }

    public void test_createAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync("Folder100", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                result = driven.id("Folder100");
                assertNotNull(result);
                assertTrue(result.isDirectory());

                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_create_file() throws Exception {
        driven.authenticate(credential);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        DrivenFile drivenFile = driven.create("File101", fileContent);
        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
        assertEquals("MimeType101", drivenFile.getType());

        drivenFile = driven.id("File101");
        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
        assertEquals("MimeType101", drivenFile.getType());
    }

    public void test_createAsync_file() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        driven.createAsync("File101", fileContent, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertFalse(result.isDirectory());
                assertEquals("MimeType101", result.getType());

                result = driven.id("File101");
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
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        assertNotNull(parent);

        DrivenFile drivenFile = driven.create(parent, "Folder110");
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
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        assertNotNull(parent);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "Folder110", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                // check parent
                // since we're mocking the fields requested are ignored
                // therefore the parent references should be populated
                // without having to have to call drivenFile.getDetails()
                assertEquals("Folder100", ((GoogleDriveFile) result).getModel().getParents().get(0).getId());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_create_fileInParent() throws Exception {
        // we're going to create a file within a parent
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        assertNotNull(parent);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        DrivenFile drivenFile = driven.create(parent, "File101", fileContent);

        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
        assertEquals(drivenFile.getType(), "MimeType101");
        assertEquals("Folder100", ((GoogleDriveFile)drivenFile).getModel().getParents().get(0).getId());
    }

    public void test_createAsync_fileInParent() throws Exception {
        // we're going to create a file within a parent
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        assertNotNull(parent);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "File101", fileContent, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertFalse(result.isDirectory());
                assertEquals(result.getType(), "MimeType101");
                assertEquals("Folder100", ((GoogleDriveFile) result).getModel().getParents().get(0).getId());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_list() throws Exception {
        driven.authenticate(credential);

        int counter = 1;
        for(DrivenFile drivenFile : driven.list()){
            assertEquals("Id0" + counter, drivenFile.getId());
            assertEquals("Title0" + counter, drivenFile.getName());
            assertEquals("MimeType0" + counter, drivenFile.getType());
            assertEquals("DownloadUrl0" + counter, drivenFile.getDownloadUrl());

            counter++;
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                int counter = 1;
                for (DrivenFile drivenFile : result) {
                    assertEquals("Id0" + counter, drivenFile.getId());
                    assertEquals("Title0" + counter, drivenFile.getName());
                    assertEquals("MimeType0" + counter, drivenFile.getType());
                    assertEquals("DownloadUrl0" + counter, drivenFile.getDownloadUrl());

                    counter++;
                }
                assertTrue(counter > 1);

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_list_Parent() throws Exception {
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        int counter = 1;
        for(DrivenFile drivenFile : driven.list(parent)){
            assertEquals("Folder1" + counter + "0", drivenFile.getName());
            counter++;
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync_Parent() throws Exception {
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(parent, new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                int counter = 1;
                for (DrivenFile drivenFile : result) {
                    assertEquals("Folder1" + counter + "0", drivenFile.getName());
                    counter++;
                }
                assertTrue(counter > 1);

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_getDetails() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("Id01");
        assertFalse(drivenFile.hasDetails());

        drivenFile = driven.getDetails(drivenFile);
        assertNotNull(drivenFile);
        assertTrue(drivenFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("Id01");
        assertFalse(drivenFile.hasDetails());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.getDetailsAsync(drivenFile, new Task<DrivenFile>() {
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
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("Id01");
        assertTrue(driven.share(drivenFile, "other-user"));
    }

    public void test_shareAsync() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("Id01");
        final CountDownLatch signal = new CountDownLatch(1);
        driven.shareAsync(drivenFile, "other-user", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });

        signal.await();
    }

}
