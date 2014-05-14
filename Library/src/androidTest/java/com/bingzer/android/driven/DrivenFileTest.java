package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.DriveUtils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class DrivenFileTest extends AndroidTestCase {

    private Driven driven;
    private DrivenFile drivenFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(Driven.class);
        GoogleAccountCredential credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");

        DrivenFile.setDriven(driven);

        driven.authenticate(credential);
        drivenFile = driven.get("Title01");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void test_property() throws Exception {
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeType01", drivenFile.getType());
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.isDirectory());
        assertFalse(drivenFile.hasDetails());

        assertEquals("Id01", drivenFile.getModel().getId());
        assertEquals("Title01", drivenFile.getModel().getTitle());
        assertEquals("MimeType01", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl01", drivenFile.getModel().getDownloadUrl());
    }

    public void test_isDirectory() throws Exception {
        assertFalse(drivenFile.isDirectory());
    }

    public void test_getDetails() throws Exception {
        drivenFile.getDetails();
        assertTrue(drivenFile.hasDetails());
    }

    public void test_getDetailsAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.getDetailsAsync(new Task<File>() {
            @Override
            public void onCompleted(File result) {
                assertTrue(drivenFile.hasDetails());
                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_delete() throws Exception {
        assertTrue(drivenFile.delete());
        assertNull(driven.get(drivenFile.getTitle()));
    }

    public void test_deleteAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.deleteAsync(new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertNull(driven.get(drivenFile.getTitle()));
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
            assertEquals("File1" + counter, df.getTitle());
        }
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
                    assertEquals("File1" + counter, df.getTitle());

                    signal.countDown();
                }
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
        FileContent fileContent = new FileContent("MimeTypeEdited01", new java.io.File(""));
        assertTrue(drivenFile.upload(fileContent));

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeTypeEdited01", drivenFile.getType());  // we changed this (was MimeType01)
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id01", drivenFile.getModel().getId());
        assertEquals("Description01", drivenFile.getModel().getDescription());
        assertEquals("MimeTypeEdited01", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl01", drivenFile.getModel().getDownloadUrl());
    }

    public void test_uploadAsync() throws Exception {
        FileContent fileContent = new FileContent("MimeTypeEdited01", new java.io.File(""));

        drivenFile.uploadAsync(fileContent, new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

                assertNotNull(drivenFile);
                assertEquals("Id01", drivenFile.getId());
                assertEquals("Title01", drivenFile.getTitle());
                assertEquals("MimeTypeEdited01", drivenFile.getType());  // we changed this (was MimeType01)
                assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
                assertFalse(drivenFile.hasDetails());

                // check raw model
                assertEquals("Id01", drivenFile.getModel().getId());
                assertEquals("Description01", drivenFile.getModel().getDescription());
                assertEquals("MimeTypeEdited01", drivenFile.getModel().getMimeType());
                assertEquals("DownloadUrl01", drivenFile.getModel().getDownloadUrl());
            }
        });
    }

}
