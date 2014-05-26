package com.bingzer.android.driven.gdrive;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.contracts.Task;

import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class GoogleDriveFileTest extends AndroidTestCase {

    private GoogleDrive driven;
    private RemoteFile remoteFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(GoogleDrive.class);
        Credential credential = new Credential(getContext(), "Test-User");

        GoogleDriveFile.setStorageProvider(driven);

        driven.authenticate(credential);
        remoteFile = driven.get("Title01");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void test_property() throws Exception {
        assertEquals("Id01", remoteFile.getId());
        assertEquals("Title01", remoteFile.getName());
        assertEquals("MimeType01", remoteFile.getType());
        assertEquals("DownloadUrl01", remoteFile.getDownloadUrl());
        assertFalse(remoteFile.hasDetails());

        assertEquals("Id01", ((GoogleDriveFile) remoteFile).getModel().getId());
        assertEquals("Title01", ((GoogleDriveFile) remoteFile).getModel().getTitle());
        assertEquals("MimeType01", ((GoogleDriveFile) remoteFile).getModel().getMimeType());
        assertEquals("DownloadUrl01", ((GoogleDriveFile) remoteFile).getModel().getDownloadUrl());
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

    public void test_list() throws Exception {
        remoteFile = driven.create("Folder10");
        assertNotNull(driven.create(remoteFile, "File11"));
        assertNotNull(driven.create(remoteFile, "File12"));

        int counter = 1;
        for(RemoteFile df : remoteFile.list()){
            assertNotNull(df);
            assertEquals("File1" + counter, df.getName());
            counter++;
        }

        assertTrue(counter > 0);
    }

    public void test_listAsync() throws Exception {
        remoteFile = driven.create("Folder10");
        assertNotNull(driven.create(remoteFile, "File11"));
        assertNotNull(driven.create(remoteFile, "File12"));

        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.listAsync(new Task<java.util.List<RemoteFile>>() {
            @Override public void onCompleted(java.util.List<RemoteFile> result) {
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

    public void test_share() throws Exception {
        assertNotNull(remoteFile.share("other-user"));
    }

    public void test_shareAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.shareAsync("other-user", new Task<String>() {
            @Override
            public void onCompleted(String result) {
                assertNotNull(result);

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_upload() throws Exception {
        LocalFile localFile = new LocalFile("MimeTypeEdited01", new java.io.File(""));
        assertTrue(remoteFile.upload(localFile));

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

    public void test_uploadAsync() throws Exception {
        LocalFile localFile = new LocalFile("MimeTypeEdited01", new java.io.File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.uploadAsync(localFile, new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

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

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_rename() throws Exception {
        assertEquals("Title01", remoteFile.getName());
        assertTrue(remoteFile.rename("Title01_Renamed"));
        assertEquals("Title01_Renamed", remoteFile.getName());

        assertNull(driven.get("Title01"));
        assertNotNull(driven.get("Title01_Renamed"));
    }

    public void test_renameAsync() throws Exception {
        assertEquals("Title01", remoteFile.getName());

        final CountDownLatch signal = new CountDownLatch(1);
        remoteFile.renameAsync("Title01_Renamed", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertEquals("Title01_Renamed", remoteFile.getName());

                assertNull(driven.get("Title01"));
                assertNotNull(driven.get("Title01_Renamed"));
                signal.countDown();
            }
        });

        signal.await();
    }

}
