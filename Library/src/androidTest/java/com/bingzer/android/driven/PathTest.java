package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.utils.Path;

public class PathTest extends AndroidTestCase{

    public void test_combine(){
        assertEquals("/", Path.combine((String) null, null));

        assertEquals("Folder/", Path.combine("Folder", null));
        assertEquals("Folder/", Path.combine("Folder", ""));

        assertEquals("/File", Path.combine((String) null, "File"));
        assertEquals("/File", Path.combine("", "File"));

        assertEquals("Folder/File", Path.combine("Folder", "File"));
    }

    public void test_clean(){
        assertEquals(null, Path.clean((String) null));
        assertEquals("/", Path.clean("/"));
        assertEquals("/", Path.clean(""));


        assertEquals("/Folder", Path.clean("Folder"));
        assertEquals("/Folder", Path.clean("/Folder"));
        assertEquals("/Folder/File", Path.clean("/Folder/File"));
        assertEquals("/Folder/File", Path.clean("Folder/File"));
    }

    public void test_getDirectory(){
        assertEquals("/Folder/SubFolder", Path.getDirectory("/Folder/SubFolder/File"));
        assertEquals("/Folder", Path.getDirectory("/Folder/SubFolder/"));
        assertEquals("/Folder", Path.getDirectory("/Folder/SubFolder"));
        assertEquals("/", Path.getDirectory("/Folder/"));
        assertEquals("/", Path.getDirectory("/Folder"));
        assertNull(Path.getDirectory("/"));
    }

    public void test_getFilename(){
        assertEquals("File", Path.getFilename("/Folder/SubFolder/File"));
        assertEquals("File.001", Path.getFilename("/Folder/SubFolder/File.001"));
        assertEquals("File", Path.getFilename("File"));
        assertEquals("File.001", Path.getFilename("File.001"));
        assertEquals("Folder", Path.getFilename("/Folder"));
        assertEquals("Folder", Path.getFilename("/Folder/"));
        assertEquals("SubFolder", Path.getFilename("/Folder/SubFolder"));
        assertEquals("SubFolder", Path.getFilename("/Folder/SubFolder/"));
        assertEquals("/", Path.getFilename("/"));
    }
}
