package com.bingzer.android.driven.dropbox;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.contracts.Task;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class DropboxFileTest extends AndroidTestCase {

    private Dropbox driven;
    private RemoteFile remoteFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(Dropbox.class);
        Credential credential = new Credential(getContext(), "Test-User");
        credential.setToken(new Credential.Token("appKey", "appSecret"));

        DropboxFile.setStorageProvider(driven);

        driven.authenticate(credential);
        remoteFile = driven.get("/Folder100/File101");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void test_property() throws Exception {
        assertEquals("/Folder100/File101", remoteFile.getId());
        assertEquals("File101", remoteFile.getName());
        assertEquals("MimeType101", remoteFile.getType());
        assertEquals("/Folder100/File101", remoteFile.getDownloadUrl());
        assertTrue(remoteFile.hasDetails());
    }

    public void test_isDirectory() throws Exception {
        assertFalse(remoteFile.isDirectory());
    }

    public void test_getDetails() throws Exception {
        remoteFile.fetchDetails();
        assertTrue(remoteFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.fetchDetailsAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(remoteFile.hasDetails());
                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_delete() throws Exception {
        assertTrue(remoteFile.delete());
        assertNull(driven.get(remoteFile.getName()));
    }

    public void test_deleteAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.deleteAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertNull(driven.get(remoteFile.getName()));
                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_get() throws Exception {
        remoteFile = driven.create("Folder88");
        assertNotNull(driven.create(remoteFile, "File11"));
        assertNotNull(driven.create(remoteFile, "File12"));

        RemoteFile child = remoteFile.get("File11");
        assertNotNull(child);
        assertEquals("File11", child.getName());
    }

    public void test_getAsync() throws Exception {
        remoteFile = driven.create("Folder88");
        assertNotNull(driven.create(remoteFile, "File11"));
        assertNotNull(driven.create(remoteFile, "File12"));

        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.getAsync("File11", new Task<RemoteFile>() {
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
        remoteFile = driven.create("Folder88");
        assertNotNull(driven.create(remoteFile, "File11"));
        assertNotNull(driven.create(remoteFile, "File12"));

        int counter = 1;
        for(RemoteFile df : remoteFile.list()){
            assertNotNull(df);
            assertEquals("File1" + counter, df.getName());
            counter++;
        }

        assertTrue(counter > 1);
    }

    public void test_listAsync() throws Exception {
        remoteFile = driven.create("Folder88");
        assertNotNull(driven.create(remoteFile, "File11"));
        assertNotNull(driven.create(remoteFile, "File12"));

        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.listAsync(new Task<List<RemoteFile>>() {
            @Override public void onCompleted(List<RemoteFile> result) {
                int counter = 1;
                for(RemoteFile df : remoteFile.list()){
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
        LocalFile local = new LocalFile("MimeType101", new java.io.File(""));
        assertTrue(remoteFile.upload(local));

        assertNotNull(remoteFile);
        assertEquals("/Folder100/File101", remoteFile.getId());
        assertEquals("File101", remoteFile.getName());
        assertEquals("MimeType101", remoteFile.getType());  // we changed this (was MimeType01)
        assertEquals("/Folder100/File101", remoteFile.getDownloadUrl());
        assertTrue(remoteFile.hasDetails());
    }

    public void test_uploadAsync() throws Exception {
        LocalFile local = new LocalFile("MimeType101", new java.io.File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.uploadAsync(local, new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

                assertEquals("/Folder100/File101", remoteFile.getId());
                assertEquals("File101", remoteFile.getName());
                assertEquals("MimeType101", remoteFile.getType());  // we changed this (was MimeType01)
                assertEquals("/Folder100/File101", remoteFile.getDownloadUrl());
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

        assertNull(driven.get("/Folder100/File101"));
        assertNotNull(driven.get("/Folder100/File101_Renamed"));
    }

    public void test_renameAsync() throws Exception {
        assertEquals("File101", remoteFile.getName());

        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.renameAsync("File101_Renamed", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertEquals("File101_Renamed", remoteFile.getName());

                assertNull(driven.get("/Folder100/File101"));
                assertNotNull(driven.get("/Folder100/File101_Renamed"));
                signal.countDown();
            }
        });

        signal.await();
    }

}
