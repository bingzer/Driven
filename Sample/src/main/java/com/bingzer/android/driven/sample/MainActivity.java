package com.bingzer.android.driven.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.dropbox.Dropbox;
import com.bingzer.android.driven.dropbox.app.DropboxActivity;
import com.bingzer.android.driven.gdrive.GoogleDrive;
import com.bingzer.android.driven.gdrive.app.GoogleDriveActivity;
import com.bingzer.android.driven.local.ExternalDrive;
import com.bingzer.android.driven.local.app.ExternalDriveActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is a quick sample activity which shows different providers.
 * The code is messy :D
 */
public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener{

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PICK_FILE = 102;
    private StorageProvider gdrive = new GoogleDrive();
    private StorageProvider dropbox = new Dropbox();
    private StorageProvider externalDrive = new ExternalDrive();
    private StorageProvider storageProvider = gdrive;

    private List<RemoteFile> files;
    private RemoteFile parent;
    private static final ArrayList<RemoteFile> breadcrumbs = new ArrayList<RemoteFile>();

    private ListAdapter listAdapter;
    private ListView listView;
    private TextView breadcrumbsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        breadcrumbsView = (TextView) findViewById(R.id.text_view);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(listAdapter = new ListAdapter());
        listView.setOnItemClickListener(new OnFileClickListener());
        listView.setOnItemLongClickListener(new OnFileLongClickListener());

        SpinnerAdapter spinnerAdapter = ArrayAdapter
                .createFromResource(this, R.array.driven_providers, android.R.layout.simple_dropdown_item_1line);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_create_folder)
            return promptNewFolder();
        else if(id == R.id.action_create_file)
            return promptForFile();
        else if(id == R.id.action_clear_credentials)
            return clearCredentials();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String provider = getBaseContext().getResources().getStringArray(R.array.driven_providers)[itemPosition];
        if(getString(R.string.google_drive).equals(provider)){
            if(!gdrive.isAuthenticated()){
                GoogleDriveActivity.launch(this);
            }
            else{
                storageProvider = gdrive;
                breadcrumbs.clear();
                list(null);
            }
            return true;
        }
        else if(getString(R.string.dropbox).equals(provider)){
            if(!dropbox.isAuthenticated()){
                DropboxActivity.launch(this, BuildConfig.DROPBOX_APP_KEY, BuildConfig.DROPBOX_APP_SECRET);
            }
            else{
                storageProvider = dropbox;
                breadcrumbs.clear();
                list(null);
            }
            return true;
        }
        else if(getString(R.string.external_drive).equals(provider)){
            if(!externalDrive.isAuthenticated()){
                ExternalDriveActivity.launch(this, Environment.getExternalStorageDirectory().getAbsolutePath());
            }
            else{
                storageProvider = externalDrive;
                breadcrumbs.clear();
                list(null);
            }
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case GoogleDriveActivity.REQUEST_LOGIN:
                    storageProvider = gdrive;
                    break;
                case DropboxActivity.REQUEST_LOGIN:
                    storageProvider = dropbox;
                    break;
                case ExternalDriveActivity.REQUEST_LOGIN:
                    storageProvider = externalDrive;
                    break;
                case REQUEST_PICK_FILE:
                    createFile("*/*", new File(data.getData().getPath()));
                    return;
            }

            breadcrumbs.clear();
            list(null);
        }

    }

    @Override
    public void onBackPressed() {
        if(breadcrumbs.size() > 0){
            try{
                RemoteFile lastParent = breadcrumbs.remove(breadcrumbs.size() - 1);
                lastParent = breadcrumbs.remove(breadcrumbs.size() - 1);
                list(lastParent);
            }
            catch (ArrayIndexOutOfBoundsException e){
                list(null);
            }
        }
        else{
            super.onBackPressed();
        }
    }

    private void list(RemoteFile parent){
        breadcrumbs.add(parent);
        this.parent = parent;
        files = null;
        listAdapter.notifyDataSetChanged();

        storageProvider.listAsync(parent, new Task<List<RemoteFile>>() {
            @Override
            public void onCompleted(List<RemoteFile> result) {
                files = result;
                listAdapter.notifyDataSetChanged();
            }
        });
        updateBreadcrumbsTextView();
    }

    private boolean promptNewFolder(){
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_folder)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = editText.getText();
                        createFolder(value.toString());
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();

        return true;
    }

    private void createFolder(String folderName){
        if(folderName != null && folderName.length() > 0) {
            storageProvider.createAsync(parent, folderName, new Task<RemoteFile>() {
                @Override
                public void onCompleted(RemoteFile result) {
                    list(parent);
                }
            });
        }
    }

    private void createFile(String type, File local){
        if(local != null && local.exists()){
            storageProvider.createAsync(parent, new LocalFile(local, type), new Task<RemoteFile>() {
                @Override
                public void onCompleted(RemoteFile result) {
                    list(parent);
                }
            });
        }
    }

    private boolean promptForFile(){
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            startActivityForResult(intent, REQUEST_PICK_FILE);
        }
        catch (Throwable e){
            Toast.makeText(this, "You need a file browser app installed to do this", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void updateBreadcrumbsTextView(){
        final StringBuilder text = new StringBuilder();
        for(RemoteFile file : breadcrumbs){
            if(file != null)
                text.append(file.getName());
            text.append("/");
        }

        runOnUiThread(new Runnable() {
            @Override public void run() {
                breadcrumbsView.setText(text);
            }
        });
    }

    private boolean clearCredentials(){
        gdrive.clearSavedCredential(this);
        dropbox.clearSavedCredential(this);
        return true;
    }

    private void openFile(File file, String type){
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), type);

        List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, 0);
        if (infos.size() > 0) {
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Can't open file: " + file.getName(), Toast.LENGTH_SHORT).show();
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(files == null) return 0;
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                ViewHolder viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.view_file, parent, false);

                viewHolder.textView = (TextView) convertView.findViewById(R.id.text1);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image1);
                convertView.setTag(viewHolder);
            }

            RemoteFile file = files.get(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.textView.setText(file.getName());
            holder.imageView.setImageResource(file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file);

            return convertView;
        }
    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    class OnFileClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final RemoteFile file = files.get(position);
            if(file.isDirectory()){
                list(file);
            }
            else{
                File tempFile = new File(getBaseContext().getCacheDir(), UUID.randomUUID().toString());
                final LocalFile localFile = new LocalFile(tempFile);
                file.downloadAsync(localFile, new Task<Boolean>() {
                    @Override public void onCompleted(Boolean result) {
                        if(result){
                            openFile(localFile.getFile(), localFile.getType());
                        }
                        else{
                            Toast.makeText(getBaseContext(), "Failed to download " + file.getName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Toast.makeText(getBaseContext(), "You will be notified when download is finished", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class OnFileLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final RemoteFile file = files.get(position);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.confirm_delete)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            file.deleteAsync(new Task<Boolean>() {
                                @Override
                                public void onCompleted(Boolean result) {
                                    list(MainActivity.this.parent);
                                }
                            });
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
            return false;
        }
    }

}
