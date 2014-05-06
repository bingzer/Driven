package com.bingzer.android.driven;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.util.ArrayList;
import java.util.List;

public class MockGoogleDrive {

    public FileList rootList = new FileList();
    public FileList folder1List = new FileList();
    public FileList folder2List = new FileList();

    public MockGoogleDrive(){
        // -- ROOT
        File root = new File().setId("0").setMimeType(DriveFile.MIME_TYPE_FOLDER).setDownloadUrl("download0").setTitle("title0").setEtag("etag0").setDescription("description0");
        rootList.getItems().add(new File().setId("0-1").setMimeType("Mime0-1").setDownloadUrl("download0-1").setTitle("title0-1").setEtag("etag0-1").setDescription("description0-1").setParents(folder(root)));
        rootList.getItems().add(new File().setId("0-2").setMimeType("Mime0-2").setDownloadUrl("download0-2").setTitle("title0-2").setEtag("etag0-2").setDescription("description0-2").setParents(folder(root)));

        // -- FOLDER 1
        File folder = new File().setId("1").setMimeType(DriveFile.MIME_TYPE_FOLDER).setDownloadUrl("download1").setTitle("title1").setEtag("etag1").setDescription("description1").setParents(folder(root));
        rootList.getItems().add(folder);
        folder1List.getItems().add(new File().setId("1-1").setMimeType("Mime1-1").setDownloadUrl("download1-1").setTitle("title1-1").setEtag("etag1-1").setDescription("description1-1").setParents(folder(folder)));
        folder1List.getItems().add(new File().setId("1-2").setMimeType("Mime1-2").setDownloadUrl("download1-2").setTitle("title1-2").setEtag("etag1-2").setDescription("description1-2").setParents(folder(folder)));
        folder1List.getItems().add(new File().setId("1-3").setMimeType("Mime1-3").setDownloadUrl("download1-3").setTitle("title1-3").setEtag("etag1-3").setDescription("description1-3").setParents(folder(folder)));
        folder1List.getItems().add(new File().setId("1-4").setMimeType("Mime1-4").setDownloadUrl("download1-4").setTitle("title1-4").setEtag("etag1-4").setDescription("description1-4").setParents(folder(folder)));
        folder1List.getItems().add(new File().setId("1-5").setMimeType("Mime1-5").setDownloadUrl("download1-5").setTitle("title1-5").setEtag("etag1-5").setDescription("description1-5").setParents(folder(folder)));

        // -- FOLDER 2
        folder = new File().setId("2").setMimeType(DriveFile.MIME_TYPE_FOLDER).setDownloadUrl("download2").setTitle("title2").setEtag("etag2").setDescription("description2").setParents(folder(root));
        rootList.getItems().add(folder);
        folder2List.getItems().add(new File().setId("2-1").setMimeType("Mime2-1").setDownloadUrl("download2-1").setTitle("title2-1").setEtag("etag2-1").setDescription("description2-1").setParents(folder(folder)));
        folder2List.getItems().add(new File().setId("2-2").setMimeType("Mime2-2").setDownloadUrl("download2-2").setTitle("title2-2").setEtag("etag2-2").setDescription("description2-2").setParents(folder(folder)));

    }

    private List<ParentReference> folder(File folder){
        List<ParentReference> parents = new ArrayList<ParentReference>();
        parents.add(new ParentReference().setId(folder.getId()));
        return parents;
    }
}
