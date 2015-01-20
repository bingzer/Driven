package com.bingzer.android.driven.local;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.contracts.Task;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExternalDriveTest extends AndroidTestCase {

    File rootFile;
    ExternalDrive driven;
    Credential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        rootFile = new File(Environment.getExternalStorageDirectory(), "driven-extdrive-test");
        FileGenerator.generate(rootFile);

        driven = new ExternalDrive();
        credential = new Credential(getContext(), rootFile.getAbsolutePath());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        driven.clearSavedCredential(getContext());

        FileGenerator.clean(rootFile);
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

        driven.clearSavedCredential(getContext());
        assertTrue(driven.isAuthenticated());
    }


    public void test_deauthenticateAsync() throws InterruptedException {
        driven.authenticate(credential);
        assertTrue(driven.isAuthenticated());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.clearSavedCredentialAsync(getContext(), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertTrue(driven.isAuthenticated());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_authenticate_NoSave(){
        Result<DrivenException> result = driven.authenticate(credential);

        assertTrue(driven.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
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
        RemoteFile remoteFile = driven.get(driven.get("Folder100"), "File102");

        assertNotNull(remoteFile);
        assertEquals("File102", remoteFile.getName());
        assertTrue(remoteFile.hasDetails());
    }

    public void test_getAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("Folder100/File102", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile remoteFile) {
                assertNotNull(remoteFile);
                assertEquals("File102", remoteFile.getName());
                assertTrue(remoteFile.hasDetails());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        driven.authenticate(credential);
        RemoteFile remoteFile = driven.get("/Folder100/File103");

        assertNotNull(remoteFile);
        assertEquals("File103", remoteFile.getName());
        assertTrue(remoteFile.hasDetails());
    }

    public void test_titleAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("/Folder100/File103", new Task.WithErrorReporting<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertNotNull(result);
                assertEquals("File103", result.getName());
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
        RemoteFile remoteFile = driven.get("File001");
        LocalFile localFile = new LocalFile(new File(remoteFile.getId()));

        driven.update(remoteFile, localFile);
        remoteFile = driven.id("File001");

        assertNotNull(remoteFile);
        assertEquals("File001", remoteFile.getName());
        assertTrue(remoteFile.hasDetails());
    }

    public void test_updateAsync() throws Exception{
        driven.authenticate(credential);
        RemoteFile remoteFile = driven.get("File001");
        LocalFile localFile = new LocalFile(new File(remoteFile.getId()));

        final CountDownLatch signal = new CountDownLatch(1);
        driven.updateAsync(remoteFile, localFile, new Task.WithErrorReporting<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertNotNull(result);
                assertEquals("File001", result.getName());
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
        RemoteFile remoteFile = driven.get("File003");
        assertNotNull(remoteFile);

        assertTrue(driven.delete(remoteFile.getId()));
    }

    public void test_deleteAsync() throws Exception {
        driven.authenticate(credential);
        RemoteFile remoteFile = driven.get("File003");
        assertNotNull(remoteFile);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.deleteAsync(remoteFile.getId(), new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_first() throws Exception {
        try {
            driven.authenticate(credential);
            RemoteFile remoteFile = driven.getSearch().first("File101");
            fail("Should throw unsupported exception");
        }
        catch (UnsupportedOperationException e){
            assertTrue("good", true);
        }
    }


    public void test_query() throws Exception {
        try {
            driven.authenticate(credential);
            List<RemoteFile> remoteFiles = driven.getSearch().query("title = 'Title01'");
            fail("Should throw exception");
        }
        catch (UnsupportedOperationException e){
            assertTrue("Good", true);
        }
    }

    public void test_create() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.create("Folder200");
        assertNotNull(remoteFile);
        assertTrue(remoteFile.isDirectory());

        remoteFile = driven.id(remoteFile.getId());
        assertNotNull(remoteFile);
        assertTrue(remoteFile.isDirectory());
    }

    public void test_createAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync("Folder200", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                result = driven.id(result.getId());
                assertNotNull(result);
                assertTrue(result.isDirectory());

                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_create_file() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.get("File001");
        LocalFile localFile = new LocalFile(new File(remoteFile.getId()));

        remoteFile = driven.create(localFile);
        assertNotNull(remoteFile);
        assertFalse(remoteFile.isDirectory());

        remoteFile = driven.id("File004");
        assertNotNull(remoteFile);
        assertFalse(remoteFile.isDirectory());
    }

    public void test_createAsync_file() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.get("File001");
        LocalFile localFile = new LocalFile(new File(new File(remoteFile.getId()).getParentFile(), "File004"));

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(localFile, new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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

        RemoteFile parent = driven.create("Folder200");
        assertNotNull(parent);

        RemoteFile remoteFile = driven.create(parent, "Folder210");
        assertNotNull(remoteFile);
        assertTrue(remoteFile.isDirectory());
    }

    public void test_createAsync_inParent() throws Exception {
        // we're going to create a folder within a parent
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder200");
        assertNotNull(parent);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, "Folder210", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertNotNull(result);
                assertTrue(result.isDirectory());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_create_fileInParent() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.get("File001");
        LocalFile localFile = new LocalFile(new File(remoteFile.getId()));
        // we're going to create a file within a parent
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder200");
        assertNotNull(parent);

        remoteFile = driven.create(parent, localFile);

        assertNotNull(remoteFile);
        assertFalse(remoteFile.isDirectory());
    }

    public void test_createAsync_fileInParent() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.get("File001");
        LocalFile localFile = new LocalFile(new File(remoteFile.getId()));
        // we're going to create a file within a parent
        driven.authenticate(credential);

        RemoteFile parent = driven.create("Folder200");
        assertNotNull(parent);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.createAsync(parent, localFile, new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
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
        for(RemoteFile remoteFile : driven.list()){
            if(counter == 0) assertEquals("Folder100", remoteFile.getName());
            else if(counter == 1) assertEquals("File001", remoteFile.getName());
            else if(counter == 2) assertEquals("File002", remoteFile.getName());
            else if(counter == 3) assertEquals("File003", remoteFile.getName());

            counter++;
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(new Task<List<RemoteFile>>() {
            @Override
            public void onCompleted(List<RemoteFile> result) {
                int counter = 0;
                for(RemoteFile remoteFile : driven.list()){
                    if(counter == 0) assertEquals("Folder100", remoteFile.getName());
                    else if(counter == 1) assertEquals("File001", remoteFile.getName());
                    else if(counter == 2) assertEquals("File002", remoteFile.getName());
                    else if(counter == 3) assertEquals("File003", remoteFile.getName());

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

        RemoteFile parent = driven.get("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        int counter = 1;
        for(RemoteFile remoteFile : driven.list(parent)){
            if(remoteFile.getName().startsWith("Folder1")) {
                assertEquals("Folder1" + counter + "0", remoteFile.getName());
                counter++;
            }
        }
        assertTrue(counter > 1);
    }

    public void test_listAsync_Parent() throws Exception {
        driven.authenticate(credential);

        RemoteFile parent = driven.get("Folder100");
        driven.create(parent, "Folder110");
        driven.create(parent, "Folder120");
        driven.create(parent, "Folder130");

        final CountDownLatch signal = new CountDownLatch(1);
        driven.listAsync(parent, new Task.WithErrorReporting<List<RemoteFile>>() {
            @Override
            public void onCompleted(List<RemoteFile> result) {
                int counter = 1;
                for (RemoteFile remoteFile : result) {
                    if(remoteFile.getName().startsWith("Folder1")){
                        assertEquals("Folder1" + counter + "0", remoteFile.getName());
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

        RemoteFile remoteFile = driven.id("File001");
        assertTrue(remoteFile.hasDetails());

        remoteFile = driven.getDetails(remoteFile);
        assertNotNull(remoteFile);
        assertTrue(remoteFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        driven.authenticate(credential);

        RemoteFile remoteFile = driven.id("File001");
        assertTrue(remoteFile.hasDetails());

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
