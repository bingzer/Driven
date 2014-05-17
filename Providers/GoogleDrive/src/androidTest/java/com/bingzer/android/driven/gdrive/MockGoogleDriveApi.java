package com.bingzer.android.driven.gdrive;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.User;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockGoogleDriveApi implements GoogleDriveApi {

    public MockGoogleDriveApi(){
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
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

    private String query;
    private String updateFileId;
    private String deleteFileId;
    private FileContent updateFileContent;
    private File insertFile;

    @Override
    public Drive.Files files() {
        final Drive.Files files = mock(Drive.Files.class, RETURNS_DEEP_STUBS);

        try {
            // get()
            /////////////////////////////////////////////////////////////////////////////////////

            for(File file : fileList0.getItems()){
                when(files.get(file.getId()).setFields(anyString()).execute()).thenReturn(file);
                when(files.get(file.getId()).execute()).thenReturn(file);
            }
            /*
            when(files.get("Id01").setFields(anyString()).execute()).thenReturn(file01);
            when(files.get("Id02").setFields(anyString()).execute()).thenReturn(file02);
            when(files.get("Id03").setFields(anyString()).execute()).thenReturn(file03);
            when(files.get("Id04").setFields(anyString()).execute()).thenReturn(file04);
            when(files.get("Id05").setFields(anyString()).execute()).thenReturn(file05);
            */

            // list()
            /////////////////////////////////////////////////////////////////////////////////////
            final Drive.Files.List list = mock(Drive.Files.List.class, RETURNS_DEEP_STUBS);
            when(files.list()).thenReturn(list);

            when(list.setQ(anyString())).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    query = invocation.getArguments()[0].toString();
                    return list;
                }
            });
            when(list.execute()).then(new Answer<FileList>() {
                @Override
                public FileList answer(InvocationOnMock invocation) throws Throwable {
                    if(query != null){
                        if(query.indexOf("in parents") > 0){
                            // there's parent
                            Pattern pattern = Pattern.compile("('(.*)' in parents)");
                            Matcher matcher = pattern.matcher(query);
                            if(matcher.find()) {
                                String parentId = matcher.group(2);
                                if(parentId.equals("root")) return fileList0;

                                FileList list = new FileList();
                                list.setItems(new ArrayList<File>());

                                for (File file : fileList0.getItems()) {
                                    if (file.getParents() != null && file.getParents().size() > 0){
                                        for(ParentReference reference : file.getParents()){
                                            if(reference.getId().equals(parentId)){
                                                list.getItems().add(file);
                                            }
                                        }
                                    }
                                }

                                return list;
                            }
                        }
                        else{
                            // TODO: use regex to evaluate (ex: "title = ? AND trashed = false)
                            Pattern pattern = Pattern.compile("(title = '(.*)')");
                            Matcher matcher = pattern.matcher(query);
                            if(matcher.find()) {
                                String title = matcher.group(2);
                                for (File file : fileList0.getItems()) {
                                    if (file.getTitle().equals(title))
                                        return new FileList().setItems(Arrays.asList(file));
                                }
                            }
                        }
                    }
                    else{
                        return fileList0;
                    }

                    return null;
                }
            });

            // update()
            /////////////////////////////////////////////////////////////////////////////////////
            final Drive.Files.Update update = mock(Drive.Files.Update.class, RETURNS_DEEP_STUBS);
            when(files.update(anyString(), (File) anyObject(), (FileContent) anyObject())).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    updateFileId = invocation.getArguments()[0].toString();
                    updateFileContent = (FileContent) invocation.getArguments()[2];

                    return update;
                }
            });
            when(update.execute()).then(new Answer<File>() {
                @Override
                public File answer(InvocationOnMock invocation) throws Throwable {
                    for (File file : fileList0.getItems()) {
                        if (file.getId().equals(updateFileId)) {
                            file.setMimeType(updateFileContent.getType());
                            return file;
                        }
                    }

                    return null;
                }
            });

            // delete()
            /////////////////////////////////////////////////////////////////////////////////////
            final Drive.Files.Delete delete = mock(Drive.Files.Delete.class, RETURNS_DEEP_STUBS);
            when(files.delete(anyString())).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    deleteFileId = invocation.getArguments()[0].toString();
                    return delete;
                }
            });
            when(delete.execute()).then(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    for (File file : fileList0.getItems()) {
                        if (file.getId().equals(deleteFileId)) {
                            fileList0.getItems().remove(file);
                            return null;
                        }
                    }
                    return null;
                }
            });

            // insert()
            /////////////////////////////////////////////////////////////////////////////////////
            final Drive.Files.Insert insert = mock(Drive.Files.Insert.class, RETURNS_DEEP_STUBS);
            when(files.insert((File) anyObject())).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    insertFile = (File) invocation.getArguments()[0];
                    insertFile.setId(insertFile.getTitle()); // let's just make id the same as the title
                    fileList0.getItems().add(insertFile);

                    when(files.get(insertFile.getId()).setFields(anyString()).execute()).thenReturn(insertFile);

                    return insert;
                }
            });
            when(files.insert((File) anyObject(), (AbstractInputStreamContent) anyObject())).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    insertFile = (File) invocation.getArguments()[0];
                    insertFile.setId(insertFile.getTitle()); // let's just make id the same as the title

                    FileContent content = (FileContent) invocation.getArguments()[1];
                    insertFile.setMimeType(content.getType());

                    fileList0.getItems().add(insertFile);

                    when(files.get(insertFile.getId()).setFields(anyString()).execute()).thenReturn(insertFile);

                    return insert;
                }
            });
            when(insert.execute()).then(new Answer<File>() {
                @Override
                public File answer(InvocationOnMock invocation) throws Throwable {
                    return insertFile;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    private Permission permission;
    @Override
    public Drive.Permissions permissions() {
        final Drive.Permissions permissions = mock(Drive.Permissions.class, RETURNS_DEEP_STUBS);
        try {
            final Drive.Permissions.Insert insert = mock(Drive.Permissions.Insert.class, RETURNS_DEEP_STUBS);
            when(permissions.insert(anyString(), (Permission) anyObject())).then(new Answer<Drive.Permissions.Insert>() {
                @Override
                public Drive.Permissions.Insert answer(InvocationOnMock invocation) throws Throwable {
                    permission = (Permission) invocation.getArguments()[1];
                    return insert;
                }
            });
            when(insert.execute()).then(new Answer<Permission>() {
                @Override
                public Permission answer(InvocationOnMock invocation) throws Throwable {
                    return permission;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return permissions;
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

    private FileList fileList0 = new FileList().setItems(new ArrayList<File>(Arrays.asList(file01, file02, file03, file04, file05)));

}
