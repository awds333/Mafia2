package com.awds333.a2016.mafia.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.dialogs.NamePickDialog;
import com.awds333.a2016.mafia.dialogs.ServNamePickDialog;

public class MainActivity extends Activity {
    Button b1, b2;
    DialogFragment naimDialog, servNameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button)findViewById(R.id.newserver);
        b2 = (Button)findViewById(R.id.findeserv);
        naimDialog = new NamePickDialog();
        servNameDialog = new ServNamePickDialog();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new server creating
                servNameDialog.show(getFragmentManager(),"mytag");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //locking for server
                naimDialog.show(getFragmentManager(),"mytag");
            }
        });
    }
}
