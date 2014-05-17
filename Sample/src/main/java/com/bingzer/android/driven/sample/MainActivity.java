package com.bingzer.android.driven.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.dropbox.Dropbox;
import com.bingzer.android.driven.dropbox.app.DropboxActivity;
import com.bingzer.android.driven.gdrive.GoogleDrive;
import com.bingzer.android.driven.gdrive.app.GoogleDriveActivity;

import java.util.List;


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener{

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

            driven.listAsync(new Task<Iterable<DrivenFile>>() {
                @Override
                public void onCompleted(Iterable<DrivenFile> result) {
                    files = (List<DrivenFile>) result;
                    listAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    private List<DrivenFile> files;

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
                convertView = new TextView(getBaseContext());
            }

            DrivenFile file = files.get(position);
            ((TextView) convertView).setText(file.getName());

            return convertView;
        }
    }
}
