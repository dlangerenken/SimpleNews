package de.dala.simplenews.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import de.dala.simplenews.R;

/**
 * Created by Daniel on 30.12.13.
 */
public class HandleRSSActivity extends SherlockActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.getIntent().getDataString()!=null)
        {
            String path = this.getIntent().getDataString();
            //TODO handle this!!
            Toast.makeText(this, path + " clicked", Toast.LENGTH_SHORT).show();
        }
    }
}
