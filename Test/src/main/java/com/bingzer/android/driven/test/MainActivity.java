package com.bingzer.android.driven.test;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.app.DrivenActivity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


public class MainActivity extends ActionBarActivity {

    private TextView textView;
    private Thread testThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        testThread = new Thread(new TestRunnable());
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

    public void login(View sender){
        DrivenActivity.launch(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DrivenActivity.REQUEST_LOGIN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Successfully authenticate\n" + Driven.getDriven().getDrivenUser(), Toast.LENGTH_SHORT).show();
                textView.setVisibility(View.VISIBLE);
                testThread.start();
            }
            else{
                Toast.makeText(this, "Failed to authenticate", Toast.LENGTH_SHORT).show();
                textView.setVisibility(View.GONE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private void setText(final CharSequence text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    private void appendText(final CharSequence text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(text);
            }
        });
    }

    private void appendTextLine(final CharSequence text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendText("\n" + text);
            }
        });
    }

    class TestRunnable implements Runnable{
        @Override public void run() {
            Driven driven = Driven.getDriven();
            setText("Running test...");

            // -------------------------------------------------------------------//
            // create folder
            appendTextLine("Preparing your Google Drive for testing...");

            appendTextLine("driven.create(String name)");
            DrivenFile root;
            if((root = driven.title("DrivenTest")) == null) {
                root = driven.create("DrivenTest");
                assertTrue(root.isDirectory());
            }
            appendText("... [OK]");

            appendTextLine("driven.create(DriveFile parent, String name)");
            assertNotNull(driven.create(root, "Folder 1"));
            appendText("... [OK]");
        }
    }
}
