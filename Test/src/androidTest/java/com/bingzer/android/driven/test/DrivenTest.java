package com.bingzer.android.driven.test;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.DriveFile;
import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.MockGoogleDrive;
import com.google.api.services.drive.Drive;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DrivenTest extends AndroidTestCase{
    Driven driven;
    Drive service;
    MockGoogleDrive mockDrive;

    @Override
    protected void setUp() throws Exception {
        mockDrive = new MockGoogleDrive();
        service = mock(Drive.class, RETURNS_DEEP_STUBS);
        driven = Driven.getDriven();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void testAuthenticate(){
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void testGet() throws Exception{
        when(service.files().get("1-1").execute()).thenReturn(mockDrive.folder1List.getItems().get(0));
        when(service.files().get("1-2").execute()).thenReturn(mockDrive.folder1List.getItems().get(1));
        when(service.files().get("1-3").execute()).thenReturn(mockDrive.folder1List.getItems().get(2));

        DriveFile driveFile = driven.get("1-1");
        assertEquals(driveFile.getId(), "1-1");
        assertEquals(driveFile.getTitle(), "title1-1");
        assertEquals(driveFile.getType(), "mime1-1");
    }
}
