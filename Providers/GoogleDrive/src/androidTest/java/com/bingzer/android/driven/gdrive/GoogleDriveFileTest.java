package com.bingzer.android.driven.gdrive;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Task;

import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class GoogleDriveFileTest extends AndroidTestCase {

    private GoogleDrive driven;
    private DrivenFile drivenFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(GoogleDrive.class);
        DrivenCredential credential = new DrivenCredential(getContext(), "Test-User");

        GoogleDriveFile.setDriven(driven);

        driven.authenticate(credential);
        drivenFile = driven.get("Title01");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void test_property() throws Exception {
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getName());
        assertEquals("MimeType01", drivenFile.getType());
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        assertEquals("Id01", ((GoogleDriveFile)drivenFile).getModel().getId());
        assertEquals("Title01", ((GoogleDriveFile)drivenFile).getModel().getTitle());
        assertEquals("MimeType01", ((GoogleDriveFile)drivenFile).getModel().getMimeType());
        assertEquals("DownloadUrl01", ((GoogleDriveFile)drivenFile).getModel().getDownloadUrl());
    }

    public void test_isDirectory() throws Exception {
        assertFalse(drivenFile.isDirectory());
    }

    public void test_getDetails() throws Exception {
        drivenFile.fetchDetails();
        assertTrue(drivenFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.fetchDetailsAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(drivenFile.hasDetails());
                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_delete() throws Exception {
        assertTrue(drivenFile.delete());
        assertNull(driven.get(drivenFile.getName()));
    }

    public void test_deleteAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.deleteAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertNull(driven.get(drivenFile.getName()));
                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_list() throws Exception {
        drivenFile = driven.create("Folder10");
        assertNotNull(driven.create(drivenFile, "File11"));
        assertNotNull(driven.create(drivenFile, "File12"));

        int counter = 1;
        for(DrivenFile df : drivenFile.list()){
            assertNotNull(df);
            assertEquals("File1" + counter, df.getName());
            counter++;
        }

        assertTrue(counter > 0);
    }

    public void test_listAsync() throws Exception {
        drivenFile = driven.create("Folder10");
        assertNotNull(driven.create(drivenFile, "File11"));
        assertNotNull(driven.create(drivenFile, "File12"));

        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.listAsync(new Task<Iterable<DrivenFile>>() {
            @Override public void onCompleted(Iterable<DrivenFile> result) {
                int counter = 1;
                for(DrivenFile df : drivenFile.list()){
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
        assertTrue(drivenFile.share("other-user"));
    }

    public void test_shareAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.shareAsync("other-user", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_upload() throws Exception {
        assertTrue(drivenFile.upload("MimeTypeEdited01", new java.io.File("")));

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

    public void test_uploadAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.uploadAsync("MimeTypeEdited01", new java.io.File(""), new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

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

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_rename() throws Exception {
        assertEquals("Title01", drivenFile.getName());
        assertTrue(drivenFile.rename("Title01_Renamed"));
        assertEquals("Title01_Renamed", drivenFile.getName());

        assertNull(driven.get("Title01"));
        assertNotNull(driven.get("Title01_Renamed"));
    }

    public void test_renameAsync() throws Exception {
        assertEquals("Title01", drivenFile.getName());

        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.renameAsync("Title01_Renamed", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertEquals("Title01_Renamed", drivenFile.getName());

                assertNull(driven.get("Title01"));
                assertNotNull(driven.get("Title01_Renamed"));
                signal.countDown();
            }
        });

        signal.await();
    }

}
