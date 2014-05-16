package com.bingzer.android.driven.dropbox;

import com.bingzer.android.driven.api.Path;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.MockAccount;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.session.Session;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockDropboxApiFactory implements DropboxApiFactory {

    List<DropboxAPI.Entry> entryList;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Session> DropboxAPI<T> createApi(T session) {
        entryList = getEntries();

        DropboxAPI<T> api = mock(DropboxAPI.class, RETURNS_DEEP_STUBS);
        try {
            when(api.getSession()).thenReturn(session);
            when(api.accountInfo()).then(new Answer<DropboxAPI.Account>(){
                @Override public DropboxAPI.Account answer(InvocationOnMock invocation) throws Throwable {
                    return new MockAccount("country", "DisplayName", 1234, "referralLink", 100, 100, 100);
                }
            });

            //////////////////////////////////////////////////////////////////////
            // DELETE
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    String path = invocation.getArguments()[0].toString();

                    DropboxAPI.Entry entry = getEntryByPath(path);
                    entryList.remove(entry);
                    return null;
                }
            }).when(api).delete(anyString());

            //////////////////////////////////////////////////////////////////////
            // GETFILE
            when(api.getFile(anyString(), anyString(), (OutputStream) anyObject(), (ProgressListener) anyObject()))
                    .then(new Answer<DropboxAPI.DropboxFileInfo>() {
                        @Override
                        public DropboxAPI.DropboxFileInfo answer(InvocationOnMock invocation) throws Throwable {
                            String path = invocation.getArguments()[0].toString();

                            DropboxAPI.DropboxFileInfo info = mock(DropboxAPI.DropboxFileInfo.class, RETURNS_DEEP_STUBS);
                            when(info.getMetadata()).thenReturn(getEntryByPath(path));

                            return info;
                        }
                    });


            //////////////////////////////////////////////////////////////////////
            // METADATA
            when(api.metadata(anyString(), anyInt(), anyString(), anyBoolean(), anyString()))
                    .then(new Answer<DropboxAPI.Entry>(){
                        @Override
                        public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                            String path = invocation.getArguments()[0].toString();
                            boolean isList = (Boolean) invocation.getArguments()[3];

                            DropboxAPI.Entry entry;
                            if(path.equals(Path.ROOT))
                                entry = getRoot();
                            else
                                entry = getEntryByPath(path);

                            if(entry != null && entry.isDir && isList){
                                entry.contents = getChildren(entry);
                            }

                            return entry;
                        }
                    });


            //////////////////////////////////////////////////////////////////////
            // CREATE FOLDER
            when(api.createFolder(anyString())).then(new Answer<DropboxAPI.Entry>() {
                @Override
                public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                    String path = invocation.getArguments()[0].toString();
                    DropboxAPI.Entry entry = entry(path, "Directory", true);
                    entryList.add(entry);

                    return entry;
                }
            });


            //////////////////////////////////////////////////////////////////////
            // PUT FILE
            when(api.putFile(anyString(), (InputStream) anyObject(), anyLong(), anyString(), (ProgressListener) anyObject()))
                    .then(new Answer<DropboxAPI.Entry>() {
                @Override
                public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                    String path = invocation.getArguments()[0].toString();
                    DropboxAPI.Entry entry = entry(path, "MimeType", false);
                    entryList.add(entry);
                    return entry;
                }
            });


            //////////////////////////////////////////////////////////////////////
            // PUT FILE OVERWRITE
            when(api.putFileOverwrite(anyString(), (InputStream) anyObject(), anyLong(), (ProgressListener) anyObject()))
                    .then(new Answer<DropboxAPI.Entry>() {
                        @Override
                        public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                            String path = invocation.getArguments()[0].toString();
                            DropboxAPI.Entry entry = entry(path, "MimeType", false);
                            entryList.add(entry);
                            return entry;
                        }
                    });


            //////////////////////////////////////////////////////////////////////
            // SEARCH
            when(api.search(anyString(), anyString(), anyInt(), anyBoolean()))
                    .then(new Answer<List<DropboxAPI.Entry>>() {
                        @Override
                        public List<DropboxAPI.Entry> answer(InvocationOnMock invocation) throws Throwable {
                            String path = invocation.getArguments()[0].toString();
                            String query = invocation.getArguments()[1].toString();

                            if(path.equals(Path.ROOT) && query == null){
                                return entryList;
                            }
                            else{
                                List<DropboxAPI.Entry> entries = new ArrayList<DropboxAPI.Entry>();
                                for(DropboxAPI.Entry entry : entryList){
                                    if(entry.path.contains(query))
                                        entries.add(entry);
                                }
                                return entries;
                            }
                        }
                    });


            //////////////////////////////////////////////////////////////////////
            // SHARE
            when(api.share(anyString()))
                    .then(new Answer<DropboxAPI.Entry>() {
                        @Override
                        public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                            return null;
                        }
                    });
        }
        catch (Exception e){
            fail(e.getMessage());
        }

        return api;
    }

    @Override
    public OutputStream createOutputStream(File file) throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override
    public InputStream createInputStream(File file) throws IOException {
        return new ByteArrayInputStream(new byte[1024]);
    }

    public DropboxAPI.Entry getEntryByPath(String path){
        for(DropboxAPI.Entry entry : entryList){
            if(entry.path.equals(path)){
                return entry;
            }
        }
        return null;
    }

    public DropboxAPI.Entry getRoot(){
        return entry("/", "Directory", true);
    }

    private List<DropboxAPI.Entry> getEntries() {
        List<DropboxAPI.Entry> entries = new ArrayList<DropboxAPI.Entry>();

        entries.add(entry("/Folder100", "Directory", true));
        entries.add(entry("/Folder100/File101", "MimeType101", false));
        entries.add(entry("/Folder100/File102", "MimeType102", false));
        entries.add(entry("/Folder100/File103", "MimeType103", false));
        entries.add(entry("/File001",   "MimeType001", false));
        entries.add(entry("/File002",   "MimeType001", false));
        entries.add(entry("/File003",   "MimeType001", false));

        return entries;
    }

    private List<DropboxAPI.Entry> getChildren(DropboxAPI.Entry entry){
        if(entry.path.equals(Path.ROOT)) return entryList;

        List<DropboxAPI.Entry> entries = new ArrayList<DropboxAPI.Entry>();
        for(DropboxAPI.Entry child : entryList){
            // make sure we don't include ourself
            // check it with equals()
            if(!child.path.equals(entry.path) && child.path.contains(entry.path))
                entries.add(child);
        }

        return entries;
    }


    private DropboxAPI.Entry entry(String path, String mimeType, boolean isDir){
        DropboxAPI.Entry entry = new DropboxAPI.Entry();
        entry.isDir = isDir;
        entry.path = path;
        entry.mimeType = mimeType;

        return entry;
    }
}
