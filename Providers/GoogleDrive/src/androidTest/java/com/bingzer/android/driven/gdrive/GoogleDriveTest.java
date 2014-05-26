package com.bingzer.android.driven.gdrive;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.contracts.Task;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class GoogleDriveTest extends AndroidTestCase{

    private GoogleDrive driven;
    private Credential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(GoogleDrive.class);
        //credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");
        credential = new Credential(getContext(), "Test-User");
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
        driven.getAsync("Title01", new Task.WithErrorReporting<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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

        RemoteFile parent = driven.create("Folder100");
        driven.create(parent, "File101");

        assertTrue(driven.exists(parent, "File101"));
    }

    public void test_existsAsync_inParent() throws Exception {
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder100");
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
        RemoteFile remoteFile = driven.id("Id01");

        assertNotNull(remoteFile);
        assertEquals("Id01", remoteFile.getId());
        assertEquals("Title01", remoteFile.getName());
        assertEquals("MimeType01", remoteFile.getType());
        assertEquals("DownloadUrl01", remoteFile.getDownloadUrl());
        assertFalse(remoteFile.hasDetails());

        // check raw model
        assertEquals("Id01", ((GoogleDriveFile) remoteFile).getModel().getId());
        assertEquals("Description01", ((GoogleDriveFile) remoteFile).getModel().getDescription());
        assertEquals("MimeType01", ((GoogleDriveFile) remoteFile).getModel().getMimeType());
        assertEquals("DownloadUrl01", ((GoogleDriveFile) remoteFile).getModel().getDownloadUrl());
    }

    public void test_getAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.idAsync("Id03", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile remoteFile) {
                assertNotNull(remoteFile);
                assertEquals("Id03", remoteFile.getId());
                assertEquals("Title03", remoteFile.getName());
                assertEquals("MimeType03", remoteFile.getType());
                assertEquals("DownloadUrl03", remoteFile.getDownloadUrl());
                assertFalse(remoteFile.hasDetails());

                // check raw model
                assertEquals("Id03", ((GoogleDriveFile) remoteFile).getModel().getId());
                assertEquals("Description03", ((GoogleDriveFile) remoteFile).getModel().getDescription());
                assertEquals("MimeType03", ((GoogleDriveFile) remoteFile).getModel().getMimeType());
                assertEquals("DownloadUrl03", ((GoogleDriveFile) remoteFile).getModel().getDownloadUrl());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        driven.authenticate(credential);
        RemoteFile remoteFile = driven.get("Title02");

        assertNotNull(remoteFile);
        assertEquals("Id02", remoteFile.getId());
        assertEquals("Title02", remoteFile.getName());
        assertEquals("MimeType02", remoteFile.getType());
        assertEquals("DownloadUrl02", remoteFile.getDownloadUrl());
        assertFalse(remoteFile.hasDetails());

        // check raw model
        assertEquals("Id02", ((GoogleDriveFile) remoteFile).getModel().getId());
        assertEquals("Description02", ((GoogleDriveFile) remoteFile).getModel().getDescription());
        assertEquals("MimeType02", ((GoogleDriveFile) remoteFile).getModel().getMimeType());
        assertEquals("DownloadUrl02", ((GoogleDriveFile) remoteFile).getModel().getDownloadUrl());
    }

    public void test_titleAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("Title01", new Task.WithErrorReporting<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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
        RemoteFile remoteFile = driven.id("Id01");
        assertNotNull(remoteFile);

        driven.update(remoteFile, new LocalFile("MimeTypeEdited01", new File("")));
        remoteFile = driven.id("Id01");

        assertNotNull(remoteFile);
        assertEquals("Id01", remoteFile.getId());
        assertEquals("Title01", remoteFile.getName());
        assertEquals("MimeTypeEdited01", remoteFile.getType());  // we changed this (was MimeType01)
        assertEquals("DownloadUrl01", remoteFile.getDownloadUrl());
        assertFalse(remoteFile.hasDetails());

        // check raw model
        assertEquals("Id01", ((GoogleDriveFile) remoteFile).getModel().getId());
        assertEquals("Description01", ((GoogleDriveFile) remoteFile).getModel().getDescription());
        assertEquals("MimeTypeEdited01", ((GoogleDriveFile) remoteFile).getModel().getMimeType());
        assertEquals("DownloadUrl01", ((GoogleDriveFile) remoteFile).getModel().getDownloadUrl());
    }

    public void test_updateAsync() throws Exception{
        driven.authenticate(credential);
        RemoteFile remoteFile = driven.id("Id03");
        assertNotNull(remoteFile);

        final LocalFile localFile = new LocalFile("MimeTypeEdited03", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.updateAsync(remoteFile, localFile, new Task.WithErrorReporting<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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
        RemoteFile remoteFile = driven.id("Id03");
        assertNotNull(remoteFile);

        assertTrue(driven.delete("Id03"));
    }

    public void test_deleteAsync() throws Exception {
        driven.authenticate(credential);
        RemoteFile remoteFile = driven.id("Id02");
        assertNotNull(remoteFile);

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
        RemoteFile remoteFile = driven.first("title = 'Title01'");
        assertNotNull(remoteFile);

        assertEquals("Id01", remoteFile.getId());
        assertEquals("Title01", remoteFile.getName());
        assertEquals("MimeType01", remoteFile.getType());  // we changed this (was MimeType03)
        assertEquals("DownloadUrl01", remoteFile.getDownloadUrl());
        assertFalse(remoteFile.hasDetails());
    }

    public void test_firstAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.firstAsync("title = 'Title01'", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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
        java.util.List<RemoteFile> remoteFiles = driven.query("title = 'Title01'");
        for(RemoteFile remoteFile : remoteFiles){
            assertNotNull(remoteFile);

            assertEquals("Id01", remoteFile.getId());
            assertEquals("Title01", remoteFile.getName());
            assertEquals("MimeType01", remoteFile.getType());  // we changed this (was MimeType03)
            assertEquals("DownloadUrl01", remoteFile.getDownloadUrl());
            assertFalse(remoteFile.hasDetails());
        }
    }

    public void test_queryAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.queryAsync("title = 'Title01'", new Task<java.util.List<RemoteFile>>() {
            @Override
            public void onCompleted(java.util.List<RemoteFile> result) {
                for (RemoteFile remoteFile : result) {
                    assertNotNull(remoteFile);

                    assertEquals("Id01", remoteFile.getId());
                    assertEquals("Title01", remoteFile.getName());
                    assertEquals("MimeType01", remoteFile.getType());  // we changed this (was MimeType03)
                    assertEquals("DownloadUrl01", remoteFile.getDownloadUrl());
                    assertFalse(remoteFile.hasDetails());
                }
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_create() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.create("Folder100");
        assertNotNull(remoteFile);
        assertTrue(remoteFile.isDirectory());

        remoteFile = driven.id("Folder100");
        assertNotNull(remoteFile);
        assertTrue(remoteFile.isDirectory());
    }

    public void test_createAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync("Folder100", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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

        LocalFile localFile = new LocalFile("MimeType101", new File(""));
        RemoteFile remoteFile = driven.create("File101", localFile);
        assertNotNull(remoteFile);
        assertFalse(remoteFile.isDirectory());
        assertEquals("MimeType101", remoteFile.getType());

        remoteFile = driven.id("File101");
        assertNotNull(remoteFile);
        assertFalse(remoteFile.isDirectory());
        assertEquals("MimeType101", remoteFile.getType());
    }

    public void test_createAsync_file() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        LocalFile localFile = new LocalFile("MimeType101", new File(""));
        driven.createAsync("File101", localFile, new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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

        RemoteFile parent = driven.create("Folder100");
        assertNotNull(parent);

        RemoteFile remoteFile = driven.create(parent, "Folder110");
        assertNotNull(remoteFile);
        assertTrue(remoteFile.isDirectory());

        // check parent
        // since we're mocking the fields requested are ignored
        // therefore the parent references should be populated
        // without having to have to call drivenFile.getDetails()
        assertEquals("Folder100", ((GoogleDriveFile) remoteFile).getModel().getParents().get(0).getId());
    }

    public void test_createAsync_inParent() throws Exception {
        // we're going to create a folder within a parent
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder100");
        assertNotNull(parent);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "Folder110", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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

        RemoteFile parent = driven.create("Folder100");
        assertNotNull(parent);

        LocalFile localFile = new LocalFile("MimeType101", new File(""));
        RemoteFile remoteFile = driven.create(parent, "File101", localFile);

        assertNotNull(remoteFile);
        assertFalse(remoteFile.isDirectory());
        assertEquals(remoteFile.getType(), "MimeType101");
        assertEquals("Folder100", ((GoogleDriveFile) remoteFile).getModel().getParents().get(0).getId());
    }

    public void test_createAsync_fileInParent() throws Exception {
        // we're going to create a file within a parent
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder100");
        assertNotNull(parent);

        LocalFile localFile = new LocalFile("MimeType101", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "File101", localFile, new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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
        for(RemoteFile remoteFile : driven.list()){
            assertEquals("Id0" + counter, remoteFile.getId());
            assertEquals("Title0" + counter, remoteFile.getName());
            assertEquals("MimeType0" + counter, remoteFile.getType());
            assertEquals("DownloadUrl0" + counter, remoteFile.getDownloadUrl());

            counter++;
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(new Task<java.util.List<RemoteFile>>() {
            @Override
            public void onCompleted(java.util.List<RemoteFile> result) {
                int counter = 1;
                for (RemoteFile remoteFile : result) {
                    assertEquals("Id0" + counter, remoteFile.getId());
                    assertEquals("Title0" + counter, remoteFile.getName());
                    assertEquals("MimeType0" + counter, remoteFile.getType());
                    assertEquals("DownloadUrl0" + counter, remoteFile.getDownloadUrl());

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

        RemoteFile parent = driven.create("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        int counter = 1;
        for(RemoteFile remoteFile : driven.list(parent)){
            assertEquals("Folder1" + counter + "0", remoteFile.getName());
            counter++;
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync_Parent() throws Exception {
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(parent, new Task<java.util.List<RemoteFile>>() {
            @Override
            public void onCompleted(java.util.List<RemoteFile> result) {
                int counter = 1;
                for (RemoteFile remoteFile : result) {
                    assertEquals("Folder1" + counter + "0", remoteFile.getName());
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

        RemoteFile remoteFile = driven.id("Id01");
        assertFalse(remoteFile.hasDetails());

        remoteFile = driven.getDetails(remoteFile);
        assertNotNull(remoteFile);
        assertTrue(remoteFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.id("Id01");
        assertFalse(remoteFile.hasDetails());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.getDetailsAsync(remoteFile, new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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

        RemoteFile remoteFile = driven.id("Id01");
        assertNotNull(driven.getSharing().share(remoteFile, "other-user"));
    }

    public void test_shareAsync() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.id("Id01");
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getSharing().shareAsync(remoteFile, "other-user", new Task<String>() {
            @Override
            public void onCompleted(String result) {
                assertNotNull(result);
                signal.countDown();
            }
        });

        signal.await();
    }

}
