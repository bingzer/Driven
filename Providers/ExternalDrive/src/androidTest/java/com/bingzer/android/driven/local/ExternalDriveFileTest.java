package com.bingzer.android.driven.local;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.Suppress;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.contracts.Task;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("ALL")
@Suppress
public class ExternalDriveFileTest extends AndroidTestCase {

    File rootFile;
    private ExternalDrive driven;
    private RemoteFile remoteDir;
    private RemoteFile remoteFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        rootFile = new File(Environment.getExternalStorageDirectory(), "driven-extdrive-test");
        FileGenerator.generate(rootFile);

        driven = new ExternalDrive();
        Credential credential = new Credential(getContext(), rootFile.getAbsolutePath());

        driven.authenticate(credential);
        remoteDir = driven.get("Folder100");
        remoteFile = driven.get(remoteDir, "File101");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        FileGenerator.clean(rootFile);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void test_property() throws Exception {
        assertEquals(Environment.getExternalStorageDirectory() + "/driven-extdrive-test/Folder100", remoteDir.getId());
        assertEquals("Folder100", remoteDir.getName());
        assertEquals(Environment.getExternalStorageDirectory() + "/driven-extdrive-test/Folder100", remoteDir.getDownloadUrl());
        assertTrue(remoteDir.hasDetails());
    }

    public void test_isDirectory() throws Exception {
        assertFalse(remoteFile.isDirectory());
        assertTrue(remoteDir.isDirectory());
    }

    public void test_getDetails() throws Exception {
        remoteDir.fetchDetails();
        assertTrue(remoteDir.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        remoteDir.fetchDetailsAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(remoteDir.hasDetails());
                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_delete() throws Exception {
        assertTrue(remoteDir.delete());
        assertNull(driven.get(remoteDir.getName()));
    }

    public void test_deleteAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        remoteDir.deleteAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertNull(driven.get(remoteDir.getName()));
                signal.countDown();
            }
        });

        signal.await();
    }


    public void test_create() throws Exception {
        assertNull(driven.get(remoteDir, "Test"));

        RemoteFile child = remoteDir.create("Test");

        child = driven.get(remoteDir, "Test");
        assertTrue(child.isDirectory());
        assertNotNull(child);
        assertNotNull("Test", child.getName());
    }

    public void test_createAsync() throws Exception {
        assertNull(driven.get(remoteDir, "Test"));

        final CountDownLatch signal = new CountDownLatch(1);
        remoteDir.createAsync("Test", new Task.WithErrorReporting<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertNotNull(result);

                RemoteFile child = driven.get(remoteDir, "Test");
                assertTrue(child.isDirectory());
                assertNotNull(child);
                assertNotNull("Test", child.getName());

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

    public void test_create_localFile() throws Exception {
        File target = new File(new File(remoteFile.getId()).getParentFile(), "TestFile");
        target.delete();

        assertNull(driven.get(remoteDir, "TestFile"));

        LocalFile localFile = new LocalFile(target);
        localFile.getFile().createNewFile();

        RemoteFile child = remoteDir.create(localFile);

        assertFalse(child.isDirectory());
        assertNotNull(child);
        assertNotNull("TestFile", child.getName());
    }

    public void test_createAsync_localFile() throws Exception {
        File target = new File(new File(remoteFile.getId()).getParentFile(), "TestFile");
        target.delete();

        assertNull(driven.get(remoteDir, "TestFile"));

        LocalFile localFile = new LocalFile(target);
        localFile.getFile().createNewFile();

        final CountDownLatch signal = new CountDownLatch(1);
        remoteDir.createAsync(localFile, new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertFalse(result.isDirectory());
                assertNotNull(result);
                assertNotNull("TestFile", result.getName());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_get() throws Exception {
        remoteDir = driven.create("Folder88");
        assertNotNull(driven.create(remoteDir, "File11"));
        assertNotNull(driven.create(remoteDir, "File12"));

        RemoteFile child = remoteDir.get("File11");
        assertNotNull(child);
        assertEquals("File11", child.getName());
    }

    public void test_getAsync() throws Exception {
        remoteDir = driven.create("Folder88");
        assertNotNull(driven.create(remoteDir, "File11"));
        assertNotNull(driven.create(remoteDir, "File12"));

        final CountDownLatch signal = new CountDownLatch(1);
        remoteDir.getAsync("File11", new Task<RemoteFile>() {
            @Override
            public void onCompleted(RemoteFile result) {
                assertNotNull(result);
                assertEquals("File11", result.getName());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_list() throws Exception {
        remoteDir = driven.create("Folder88");
        assertNotNull(driven.create(remoteDir, "File11"));
        assertNotNull(driven.create(remoteDir, "File12"));

        int counter = 1;
        for(RemoteFile df : remoteDir.list()){
            assertNotNull(df);
            assertEquals("File1" + counter, df.getName());
            counter++;
        }

        assertTrue(counter > 1);
    }

    public void test_listAsync() throws Exception {
        remoteDir = driven.create("Folder88");
        assertNotNull(driven.create(remoteDir, "File11"));
        assertNotNull(driven.create(remoteDir, "File12"));

        final CountDownLatch signal = new CountDownLatch(1);
        remoteDir.listAsync(new Task<List<RemoteFile>>() {
            @Override
            public void onCompleted(List<RemoteFile> result) {
                int counter = 1;
                for (RemoteFile df : remoteDir.list()) {
                    assertNotNull(df);
                    assertEquals("File1" + counter, df.getName());

                    counter++;
                }

                assertTrue(counter > 0);
                signal.countDown();
            }
        });

        signal.await();
    }

    /*
    public void test_share() throws Exception {
        assertNotNull(drivenFile.share("other-user"));
    }

    public void test_shareAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.shareAsync("other-user", new Task<String>() {
            @Override
            public void onCompleted(String result) {
                assertNotNull(result);

                signal.countDown();
            }
        });

        signal.await();
    }
    */

    public void test_upload() throws Exception {
        LocalFile local = new LocalFile(new File(remoteFile.getId()));
        assertTrue(remoteFile.upload(local));

        assertNotNull(remoteFile);
        assertEquals("File101", remoteFile.getName());
        assertTrue(remoteFile.hasDetails());
    }

    public void test_uploadAsync() throws Exception {
        LocalFile local = new LocalFile(new File(remoteFile.getId()));
        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.uploadAsync(local, new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

                assertEquals("File101", remoteFile.getName());
                assertTrue(remoteFile.hasDetails());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_rename() throws Exception {
        assertEquals("File101", remoteFile.getName());
        assertTrue(remoteFile.rename("File101_Renamed"));
        assertEquals("File101_Renamed", remoteFile.getName());

        assertNull(driven.get(remoteDir, "File101"));
        assertNotNull(driven.get(remoteDir, "File101_Renamed"));
    }

    public void test_renameAsync() throws Exception {
        assertEquals("File101", remoteFile.getName());

        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.renameAsync("File101_Renamed", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertEquals("File101_Renamed", remoteFile.getName());

                assertNull(driven.get(remoteDir, "File101"));
                assertNotNull(driven.get(remoteDir, "File101_Renamed"));
                signal.countDown();
            }
        });

        signal.await();
    }

}
