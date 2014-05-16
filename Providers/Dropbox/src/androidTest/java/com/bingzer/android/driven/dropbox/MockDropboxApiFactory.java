package com.bingzer.android.driven.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.session.Session;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
                            return null;
                        }
                    });


            //////////////////////////////////////////////////////////////////////
            // CREATE FOLDER
            when(api.createFolder(anyString())).then(new Answer<DropboxAPI.Entry>() {
                @Override
                public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            });


            //////////////////////////////////////////////////////////////////////
            // PUT FILE
            when(api.putFile(anyString(), (InputStream) anyObject(), anyLong(), anyString(), (ProgressListener) anyObject()))
                    .then(new Answer<DropboxAPI.Entry>() {
                @Override
                public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            });


            //////////////////////////////////////////////////////////////////////
            // PUT FILE OVERWRITE
            when(api.putFileOverwrite(anyString(), (InputStream) anyObject(), anyLong(), (ProgressListener) anyObject()))
                    .then(new Answer<DropboxAPI.Entry>() {
                        @Override
                        public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                            return null;
                        }
                    });


            //////////////////////////////////////////////////////////////////////
            // SEARCH
            when(api.search(anyString(), anyString(), anyInt(), anyBoolean()))
                    .then(new Answer<DropboxAPI.Entry>() {
                        @Override
                        public DropboxAPI.Entry answer(InvocationOnMock invocation) throws Throwable {
                            return null;
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

    public DropboxAPI.Entry getEntryByPath(String path){
        for(DropboxAPI.Entry entry : entryList){
            if(entry.path.equals(path)){
                return entry;
            }
        }
        return null;
    }

    private List<DropboxAPI.Entry> getEntries() {
        List<DropboxAPI.Entry> entries = new ArrayList<DropboxAPI.Entry>();

        entries.add(entry("/Folder01", "FolderMimeType01", true));
        entries.add(entry("/File01",   "FolderMimeType01", false));
        entries.add(entry("/File02",   "FolderMimeType02", false));
        entries.add(entry("/File03",   "FolderMimeType03", false));

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
