package com.bingzer.android.driven.dropbox;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.DrivenCredential;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.dropbox.Dropbox;
import com.bingzer.android.driven.dropbox.StubModule;

import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

public class DropboxFileTest extends AndroidTestCase {

    private Dropbox driven;
    private DrivenFile drivenFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(Dropbox.class);
        DrivenCredential credential = new DrivenCredential(getContext(), "Test-User");
        credential.setToken(new DrivenCredential.Token("appKey", "appSecret"));

        DropboxFile.setDriven(driven);

        driven.authenticate(credential);
        drivenFile = driven.get("/Folder100/File101");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void test_property() throws Exception {
        assertEquals("/Folder100/File101", drivenFile.getId());
        assertEquals("File101", drivenFile.getName());
        assertEquals("MimeType101", drivenFile.getType());
        assertEquals("/Folder100/File101", drivenFile.getDownloadUrl());
        assertTrue(drivenFile.hasDetails());
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
        drivenFile = driven.create("Folder88");
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
        drivenFile = driven.create("Folder88");
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
        assertTrue(drivenFile.upload("MimeType101", new java.io.File("")));

        assertNotNull(drivenFile);
        assertEquals("/Folder100/File101", drivenFile.getId());
        assertEquals("File101", drivenFile.getName());
        assertEquals("MimeType101", drivenFile.getType());  // we changed this (was MimeType01)
        assertEquals("/Folder100/File101", drivenFile.getDownloadUrl());
        assertTrue(drivenFile.hasDetails());
    }

    public void test_uploadAsync() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.uploadAsync("MimeTypeEdited01", new java.io.File(""), new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);

                assertEquals("/Folder100/File101", drivenFile.getId());
                assertEquals("File101", drivenFile.getName());
                assertEquals("MimeType101", drivenFile.getType());  // we changed this (was MimeType01)
                assertEquals("/Folder100/File101", drivenFile.getDownloadUrl());
                assertTrue(drivenFile.hasDetails());

                signal.countDown();
            }
        });

        signal.await();
    }

    public void test_rename() throws Exception {
        assertEquals("File101", drivenFile.getName());
        assertTrue(drivenFile.rename("File101_Renamed"));
        assertEquals("File101_Renamed", drivenFile.getName());

        assertNull(driven.get("/Folder100/File101"));
        assertNotNull(driven.get("/Folder100/File101_Renamed"));
    }

    public void test_renameAsync() throws Exception {
        assertEquals("File101", drivenFile.getName());

        final CountDownLatch signal = new CountDownLatch(1);
        drivenFile.renameAsync("File101_Renamed", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                assertEquals("File101_Renamed", drivenFile.getName());

                assertNull(driven.get("/Folder100/File101"));
                assertNotNull(driven.get("/Folder100/File101_Renamed"));
                signal.countDown();
            }
        });

        signal.await();
    }

}
