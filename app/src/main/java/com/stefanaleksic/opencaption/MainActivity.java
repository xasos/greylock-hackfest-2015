package com.stefanaleksic.opencaption;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import wei.mark.standout.StandOutWindow;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StandOutWindow.closeAll(this, CaptionWindow.class);
        StandOutWindow.show(this, CaptionWindow.class, StandOutWindow.DEFAULT_ID);

        finish();

    }
}
