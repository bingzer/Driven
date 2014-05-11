package com.bingzer.android.driven;

import com.bingzer.android.driven.contracts.DrivenService;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.User;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockDrivenService implements DrivenService {

    private Drive drive;

    public MockDrivenService(){
        drive = mock(Drive.class, RETURNS_DEEP_STUBS);
    }

    @Override
    public Drive getDrive() {
        return drive;
    }

    @Override
    public Drive.About about() {
        Drive.About about = mock(Drive.About.class, RETURNS_DEEP_STUBS);

        try {
            when(about.get().setFields(anyString()).execute())
                    .thenReturn(new About().setName("Name").setUser(new User().setDisplayName("DisplayName").setEmailAddress("EmailAddress")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return about;
    }

    @Override
    public Drive.Files files() {
        Drive.Files files = mock(Drive.Files.class, RETURNS_DEEP_STUBS);

        try {
            when(files.get("01").setFields(anyString()).execute()).thenReturn(file01);
            when(files.get("02").setFields(anyString()).execute()).thenReturn(file02);
            when(files.get("03").setFields(anyString()).execute()).thenReturn(file03);
            when(files.get("04").setFields(anyString()).execute()).thenReturn(file04);
            when(files.get("05").setFields(anyString()).execute()).thenReturn(file05);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    @Override
    public Drive.Permissions permissions() {
        return null;
    }

    @Override
    public HttpRequestFactory getRequestFactory() {
        return null;
    }

    private File file01 = new File().setId("Id01").setTitle("Title01").setMimeType("MimeType01").setDownloadUrl("DownloadUrl01").setDescription("Description01").setEtag("Etag01");
    private File file02 = new File().setId("Id02").setTitle("Title02").setMimeType("MimeType02").setDownloadUrl("DownloadUrl02").setDescription("Description02").setEtag("Etag02");
    private File file03 = new File().setId("Id03").setTitle("Title03").setMimeType("MimeType03").setDownloadUrl("DownloadUrl03").setDescription("Description03").setEtag("Etag03");
    private File file04 = new File().setId("Id04").setTitle("Title04").setMimeType("MimeType04").setDownloadUrl("DownloadUrl04").setDescription("Description04").setEtag("Etag04");
    private File file05 = new File().setId("Id05").setTitle("Title05").setMimeType("MimeType05").setDownloadUrl("DownloadUrl05").setDescription("Description05").setEtag("Etag05");
}
