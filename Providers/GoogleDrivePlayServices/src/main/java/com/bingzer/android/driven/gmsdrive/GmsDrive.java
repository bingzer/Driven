package com.bingzer.android.driven.gmsdrive;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.driven.AbsStorageProvider;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.DefaultUserInfo;
import com.bingzer.android.driven.DrivenException;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.Permission;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.UserInfo;
import com.bingzer.android.driven.contracts.Search;
import com.bingzer.android.driven.contracts.SharedWithMe;
import com.bingzer.android.driven.contracts.Sharing;
import com.bingzer.android.driven.contracts.Trashed;
import com.bingzer.android.driven.utils.IOUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.plus.Plus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GoogleDrive Storage Provider that uses DriveApi inside the GooglePlayServices
 */
public class GmsDrive extends AbsStorageProvider /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener */{

    public static final int RC_SIGN_IN = 411;
    private static final String TAG = "GmsDrive";

    private static GoogleApiClient apiClient;
    private static UserInfo userInfo;
    private static DriveFolder rootFolder;

    @Override
    public UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return apiClient != null && userInfo != null;
    }

    @Override
    public Result<DrivenException> clearSavedCredential(Context context) {
        Result<DrivenException> result = new Result<>(false);
        if(apiClient != null && apiClient.isConnected())
            apiClient.disconnect();
        apiClient = null;
        rootFolder = null;
        userInfo = null;

        Credential credential = new Credential(context);
        credential.clear(getName());

        return result;
    }

    @Override
    public Result<DrivenException> authenticate(Credential credential) {
        Result<DrivenException> result = new Result<>(false);
        try {
            if(credential == null) throw new DrivenException(new IllegalArgumentException("credential cannot be null"));

            GoogleApiClient.Builder clientBuilder = createApiClientBuilder(credential.getContext());
            if(credential.hasSavedCredential(getName())){
                credential.read(getName());
                clientBuilder.setAccountName(credential.getAccountName());
            }

            apiClient = clientBuilder.build();
            ConnectionResult connResult = apiClient.blockingConnect();

            if(connResult.getErrorCode() != ConnectionResult.SUCCESS){
                if(connResult.hasResolution() && credential instanceof GmsCredential){
                    ((GmsCredential) credential).getCallingActivity().startIntentSenderForResult(connResult.getResolution().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
                }
                throw new RuntimeException("Unable to authenticate: ConnectionResult = " + connResult.getErrorCode());
            }

            // -- get account name from Google+
            credential.setAccountName(Plus.AccountApi.getAccountName(apiClient));

            userInfo = new GoogleDriveUser(credential.getAccountName(), credential.getAccountName(), null);
            rootFolder = Drive.DriveApi.getRootFolder(apiClient);

            result.setSuccess(true);

            // only save when it's not null
            if(credential.getAccountName() != null)
                credential.save(getName());
        }
        catch (Exception e){
            result.setException(new DrivenException(e));
            apiClient = null;
        }

        return result;
    }

    @Override
    public boolean exists(String name) {
        return false;
    }

    @Override
    public boolean exists(RemoteFile parent, String name) {
        return false;
    }

    @Override
    public Permission getPermission(RemoteFile remoteFile) {
        return null;
    }

    @Override
    public RemoteFile get(RemoteFile parent, String name) {
        return null;
    }

    @Override
    public RemoteFile get(String name) {
        return null;
    }

    @Override
    public RemoteFile id(String id) {
        return null;
    }

    @Override
    public RemoteFile getDetails(RemoteFile remoteFile) {
        return null;
    }

    @Override
    public List<RemoteFile> list() {
        return list(null);
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent) {
        DriveFolder folder = getDriveFolder(parent);
        return listRemoteFiles(folder);
    }

    @Override
    public RemoteFile create(String name) {
        return create(null, name);
    }

    @Override
    public RemoteFile create(LocalFile local) {
        return create(null, local);
    }

    @Override
    public RemoteFile create(RemoteFile parent, String name) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(name).build();
        DriveFolder folder = getDriveFolder(parent);
        DriveFolder.DriveFolderResult result = folder.createFolder(apiClient, changeSet).await();
        return new GmsRemoteFile(this, result.getDriveFolder().getMetadata(apiClient).await().getMetadata());
    }

    @Override
    public RemoteFile create(RemoteFile parent, LocalFile local) {
        MetadataChangeSet changeSet = createChangeset(local);
        DriveFolder folder = getDriveFolder(parent);
        DriveFolder.DriveFileResult result = folder.createFile(apiClient, changeSet, null).await();
        DriveApi.DriveContentsResult contentsResult = result.getDriveFile().open(apiClient, DriveFile.MODE_WRITE_ONLY, new DriveFile.DownloadProgressListener() {
            @Override public void onProgress(long bytesDownloaded, long bytesExpected) {
                Log.i(TAG, "Downloading content: " + bytesDownloaded + " of " + bytesExpected);
            }
        }).await();
        DriveContents contents = contentsResult.getDriveContents();

        try{
            InputStream source = new FileInputStream(local.getFile());
            OutputStream dest = new BufferedOutputStream(contents.getOutputStream());
            IOUtils.copy(source, dest);

            IOUtils.safeClose(source);
            IOUtils.safeClose(dest);

            contents.commit(apiClient, null);
        }
        catch (IOException e){
            Log.e(TAG, "When reading stream", e);
            return null;
        }

        return new GmsRemoteFile(this, result.getDriveFile().getMetadata(apiClient).await().getMetadata());
    }

    @Override
    public RemoteFile update(RemoteFile remoteFile, LocalFile content) {
        return null;
    }

    @Override
    public boolean delete(String id) {
        return false;
    }

    @Override
    public boolean download(RemoteFile remoteFile, LocalFile local) {
        return false;
    }

    @Override
    public Search getSearch() {
        return null;
    }

    @Override
    public SharedWithMe getShared() {
        return null;
    }

    @Override
    public Sharing getSharing() {
        return null;
    }

    @Override
    public Trashed getTrashed() {
        return null;
    }

    @Override
    public String getName() {
        return "Google Drive (PlayServices)";
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private GoogleApiClient.Builder createApiClientBuilder(Context context){
        return new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addApi(Plus.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER);
    }

    private List<RemoteFile> listRemoteFiles(DriveFolder folder){
        List<RemoteFile> list = new ArrayList<>();
        DriveApi.MetadataBufferResult result = folder.listChildren(apiClient).await();
        if (result.getStatus().isSuccess()){
            for (Metadata metadata : result.getMetadataBuffer()) {
                list.add(new GmsRemoteFile(this, metadata));
            }
        }
        return list;
    }

    private DriveFolder getDriveFolder(RemoteFile parent){
        if (parent == null) return rootFolder;

        return Drive.DriveApi.getFolder(apiClient, ((GmsRemoteFile) parent).getMetadata().getDriveId());
    }

    private MetadataChangeSet createChangeset(LocalFile local){
        return new MetadataChangeSet.Builder()
                .setTitle(local.getName())
                .setMimeType(local.getType())
                .build();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    class GoogleDriveUser extends DefaultUserInfo {

        protected GoogleDriveUser(String name, String displayName, String emailAddress){
            this.name = name;
            this.displayName = displayName;
            this.emailAddress = emailAddress;
        }
    }

}
