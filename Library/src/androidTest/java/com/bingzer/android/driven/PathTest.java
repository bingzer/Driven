package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.api.Path;

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
}
