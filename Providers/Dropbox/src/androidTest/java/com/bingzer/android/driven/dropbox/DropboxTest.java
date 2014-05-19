package com.bingzer.android.driven.dropbox;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.DrivenContent;
import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class DropboxTest extends AndroidTestCase {

    Dropbox driven;
    DrivenCredential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(Dropbox.class);
        //credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");
        credential = new DrivenCredential(getContext(), "Test-User");
        credential.setToken(new DrivenCredential.Token("appKey", "appSecret"));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        driven.clearAuthentication(getContext());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_authenticate(){
        Result<DrivenException> result = driven.authenticate(credential);
        if(result.getException() != null) throw result.getException();

        assertTrue(result.isSuccess());
        assertNull(result.getException());
        assertTrue(driven.isAuthenticated());
    }

    public void test_authenticateAsync() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        driven.authenticateAsync(credential, new Task.WithErrorReporting<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertTrue(driven.isAuthenticated());
                assertTrue(result.isSuccess());
                assertNull(result.getException());
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

        assertEquals("DisplayName", driven.getDrivenUser().getName());
        assertEquals("DisplayName", driven.getDrivenUser().getDisplayName());
        // we don't have email address
        //assertEquals("EmailAddress", driven.getDrivenUser().getEmailAddress());
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

        assertTrue(driven.exists("Folder100"));
        assertTrue(driven.exists("/Folder100"));
        assertTrue(driven.exists("/Folder100/File101"));
        assertTrue(driven.exists("Folder100/File101"));

        // does not exists
        assertFalse(driven.exists("Title89"));
        assertFalse(driven.exists("Title99"));
    }

    public void test_existsAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.existsAsync("/Folder100", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();

        final CountDownLatch signal2 = new CountDownLatch(1);
        driven.existsAsync("FolderNotExists10", new Task<Boolean>() {
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
        DrivenFile drivenFile = driven.id("/Folder100/File102");

        assertNotNull(drivenFile);
        assertEquals("/Folder100/File102", drivenFile.getId());
        assertEquals("File102", drivenFile.getName());
        assertEquals("MimeType102", drivenFile.getType());
        assertEquals("/Folder100/File102", drivenFile.getDownloadUrl());
        assertTrue(drivenFile.hasDetails());
    }

    public void test_getAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.idAsync("/Folder100/File102", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile drivenFile) {
                assertNotNull(drivenFile);
                assertEquals("/Folder100/File102", drivenFile.getId());
                assertEquals("File102", drivenFile.getName());
                assertEquals("MimeType102", drivenFile.getType());
                assertEquals("/Folder100/File102", drivenFile.getDownloadUrl());
                assertTrue(drivenFile.hasDetails());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("/Folder100/File103");

        assertNotNull(drivenFile);
        assertEquals("/Folder100/File103", drivenFile.getId());
        assertEquals("File103", drivenFile.getName());
        assertEquals("MimeType103", drivenFile.getType());
        assertEquals("/Folder100/File103", drivenFile.getDownloadUrl());
        assertTrue(drivenFile.hasDetails());
    }

    public void test_titleAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("/Folder100/File103", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("/Folder100/File103", result.getId());
                assertEquals("File103", result.getName());
                assertEquals("MimeType103", result.getType());
                assertEquals("/Folder100/File103", result.getDownloadUrl());
                assertTrue(result.hasDetails());
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
        DrivenFile drivenFile = driven.id("File001");
        assertNotNull(drivenFile);

        driven.update(drivenFile, new DrivenContent("MimeType001", new File("")));
        drivenFile = driven.id("File001");

        assertNotNull(drivenFile);
        assertEquals("/File001", drivenFile.getId());
        assertEquals("File001", drivenFile.getName());
        assertEquals("MimeType001", drivenFile.getType());  // we changed this (was MimeType01)
        assertEquals("/File001", drivenFile.getDownloadUrl());
        assertTrue(drivenFile.hasDetails());
    }

    public void test_updateAsync() throws Exception{
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("File001");
        assertNotNull(drivenFile);

        final DrivenContent fileContent = new DrivenContent("MimeType001", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.updateAsync(drivenFile, fileContent, new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("/File001", result.getId());
                assertEquals("File001", result.getName());
                assertEquals("MimeType001", result.getType());  // we changed this (was MimeType01)
                assertEquals("/File001", result.getDownloadUrl());
                assertTrue(result.hasDetails());
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
        DrivenFile drivenFile = driven.id("File003");
        assertNotNull(drivenFile);

        assertTrue(driven.delete("File003"));
    }

    public void test_deleteAsync() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.id("File003");
        assertNotNull(drivenFile);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.deleteAsync("File003", new Task<Boolean>() {
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
        DrivenFile drivenFile = driven.first("File101");
        assertNotNull(drivenFile);

        assertEquals("/Folder100/File101", drivenFile.getId());
        assertEquals("File101", drivenFile.getName());
        assertEquals("MimeType101", drivenFile.getType());  // we changed this (was MimeType03)
        assertEquals("/Folder100/File101", drivenFile.getDownloadUrl());
        assertTrue(drivenFile.hasDetails());
    }

    public void test_firstAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.firstAsync("File101", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);

                assertEquals("/Folder100/File101", result.getId());
                assertEquals("File101", result.getName());
                assertEquals("MimeType101", result.getType());  // we changed this (was MimeType03)
                assertEquals("/Folder100/File101", result.getDownloadUrl());
                assertTrue(result.hasDetails());
                signal.countDown();
            }
        });
        signal.await();
    }


    public void test_query() throws Exception {
        driven.authenticate(credential);
        List<DrivenFile> drivenFiles = driven.query("title = 'Title01'");
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
        driven.queryAsync("title = 'Title01'", new Task<List<DrivenFile>>() {
            @Override
            public void onCompleted(List<DrivenFile> result) {
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

        DrivenFile drivenFile = driven.create("/Folder200");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());

        drivenFile = driven.id("/Folder200");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());
    }

    public void test_createAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync("/Folder200", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                result = driven.id("/Folder200");
                assertNotNull(result);
                assertTrue(result.isDirectory());

                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_create_file() throws Exception {
        driven.authenticate(credential);

        DrivenContent fileContent = new DrivenContent("MimeType004", new File(""));
        DrivenFile drivenFile = driven.create("File004", fileContent);
        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());

        drivenFile = driven.id("File004");
        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
    }

    public void test_createAsync_file() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        DrivenContent fileContent = new DrivenContent("MimeType004", new File(""));
        driven.createAsync("File004", fileContent, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertFalse(result.isDirectory());

                result = driven.id("File004");
                assertNotNull(result);
                assertFalse(result.isDirectory());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_create_inParent() throws Exception {
        // we're going to create a folder within a parent
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder200");
        assertNotNull(parent);

        DrivenFile drivenFile = driven.create(parent, "Folder210");
        assertNotNull(drivenFile);
        assertTrue(drivenFile.isDirectory());
    }

    public void test_createAsync_inParent() throws Exception {
        // we're going to create a folder within a parent
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder200");
        assertNotNull(parent);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "Folder210", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_create_fileInParent() throws Exception {
        // we're going to create a file within a parent
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder200");
        assertNotNull(parent);

        DrivenContent fileContent = new DrivenContent("MimeType101", new File(""));
        DrivenFile drivenFile = driven.create(parent, "File201", fileContent);

        assertNotNull(drivenFile);
        assertFalse(drivenFile.isDirectory());
    }

    public void test_createAsync_fileInParent() throws Exception {
        // we're going to create a file within a parent
        driven.authenticate(credential);

        DrivenFile parent = driven.create("Folder200");
        assertNotNull(parent);

        DrivenContent fileContent = new DrivenContent("MimeType201", new File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "File201", fileContent, new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertFalse(result.isDirectory());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_list() throws Exception {
        driven.authenticate(credential);

        /*
        entries.add(entry("/Folder100", "Directory", true));
        entries.add(entry("/Folder100/File101", "MimeType101", false));
        entries.add(entry("/Folder100/File102", "MimeType102", false));
        entries.add(entry("/Folder100/File103", "MimeType103", false));
        entries.add(entry("/File001",   "MimeType001", false));
        entries.add(entry("/File002",   "MimeType001", false));
        entries.add(entry("/File003",   "MimeType001", false));
         */

        int counter = 0;
        for(DrivenFile drivenFile : driven.list()){
            if(counter == 0) assertEquals("Folder100", drivenFile.getName());
            else if(counter == 1) assertEquals("File101", drivenFile.getName());
            else if(counter == 2) assertEquals("File102", drivenFile.getName());
            else if(counter == 3) assertEquals("File103", drivenFile.getName());
            else if(counter == 4) assertEquals("File001", drivenFile.getName());
            else if(counter == 5) assertEquals("File002", drivenFile.getName());
            else if(counter == 6) assertEquals("File003", drivenFile.getName());

            counter++;
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(new Task<List<DrivenFile>>() {
            @Override
            public void onCompleted(List<DrivenFile> result) {
                int counter = 0;
                for(DrivenFile drivenFile : driven.list()){
                    if(counter == 0) assertEquals("Folder100", drivenFile.getName());
                    else if(counter == 1) assertEquals("File101", drivenFile.getName());
                    else if(counter == 2) assertEquals("File102", drivenFile.getName());
                    else if(counter == 3) assertEquals("File103", drivenFile.getName());
                    else if(counter == 4) assertEquals("File001", drivenFile.getName());
                    else if(counter == 5) assertEquals("File002", drivenFile.getName());
                    else if(counter == 6) assertEquals("File003", drivenFile.getName());

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

        DrivenFile parent = driven.get("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        int counter = 1;
        for(DrivenFile drivenFile : driven.list(parent)){
            if(drivenFile.getName().startsWith("Folder1")) {
                assertEquals("Folder1" + counter + "0", drivenFile.getName());
                counter++;
            }
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync_Parent() throws Exception {
        driven.authenticate(credential);

        DrivenFile parent = driven.get("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(parent, new Task.WithErrorReporting<List<DrivenFile>>() {
            @Override
            public void onCompleted(List<DrivenFile> result) {
                int counter = 1;
                for (DrivenFile drivenFile : result) {
                    if(drivenFile.getName().startsWith("Folder1")){
                        assertEquals("Folder1" + counter + "0", drivenFile.getName());
                        counter++;
                    }
                }

                assertTrue(counter > 1);
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
            }
        });

        signal.await();
    }

    public void test_getDetails() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("File001");
        assertTrue(drivenFile.hasDetails());

        drivenFile = driven.getDetails(drivenFile);
        assertNotNull(drivenFile);
        assertTrue(drivenFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("File001");
        assertTrue(drivenFile.hasDetails());

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

    /*
    public void test_share() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("Id01");
        assertNotNull(driven.getSharing().share(drivenFile, "other-user"));
    }

    public void test_shareAsync() throws Exception {
        driven.authenticate(credential);

        DrivenFile drivenFile = driven.id("Id01");
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getSharing().shareAsync(drivenFile, "other-user", new Task<String>() {
            @Override
            public void onCompleted(String result) {
                assertNotNull(result);
                signal.countDown();
            }
        });

        signal.await();
    }
    */
}
