package com.bingzer.android.driven.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.dropbox.Dropbox;
import com.bingzer.android.driven.dropbox.app.DropboxActivity;
import com.bingzer.android.driven.gdrive.GoogleDrive;
import com.bingzer.android.driven.gdrive.app.GoogleDriveActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener{

    private static final String TAG = "MainActivity";
    private Driven gdrive = new GoogleDrive();
    private Driven dropbox = new Dropbox();
    private Driven driven = gdrive;

    private ListAdapter listAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(listAdapter = new ListAdapter());
        listView.setOnItemClickListener(new OnFileClickListener());

        SpinnerAdapter spinnerAdapter = ArrayAdapter
                .createFromResource(this, R.array.driven_providers, android.R.layout.simple_dropdown_item_1line);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String provider = getBaseContext().getResources().getStringArray(R.array.driven_providers)[itemPosition];
        if(getString(R.string.google_drive).equals(provider)){
            GoogleDriveActivity.launch(this);
            return true;
        }
        else if(getString(R.string.dropbox).equals(provider)){
            DropboxActivity.launch(this, BuildConfig.DROPBOX_APP_KEY, BuildConfig.DROPBOX_APP_SECRET);
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case GoogleDriveActivity.REQUEST_LOGIN:
                    driven = gdrive;
                    break;
                case DropboxActivity.REQUEST_LOGIN:
                    driven = dropbox;
                    break;
            }

            breadcrumbs.clear();
            list(null);
        }

    }

    private List<DrivenFile> files;
    private DrivenFile parent;
    private ArrayList<DrivenFile> breadcrumbs = new ArrayList<DrivenFile>();

    private void list(DrivenFile parent){
        breadcrumbs.add(parent);
        this.parent = parent;
        files = null;
        listAdapter.notifyDataSetChanged();

        driven.listAsync(parent, new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                files = (List<DrivenFile>) result;
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(breadcrumbs.size() > 0){
            try{
                DrivenFile lastParent = breadcrumbs.remove(breadcrumbs.size() - 1);
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

            DrivenFile file = files.get(position);
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
            final DrivenFile file = files.get(position);
            if(file.isDirectory()){
                list(file);
            }
            else{
                try{
                    File tempFile = File.createTempFile("pre", "suffix");
                    file.downloadAsync(tempFile, new Task<File>() {
                        @Override public void onCompleted(File result) {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(result), file.getType());
                            startActivity(intent);
                        }
                    });
                    Toast.makeText(getBaseContext(), "You will be notified when download is finished", Toast.LENGTH_SHORT).show();
                }
                catch (IOException e){
                    Log.e(TAG, "When downloading a file", e);
                }
            }
        }
    }
}
