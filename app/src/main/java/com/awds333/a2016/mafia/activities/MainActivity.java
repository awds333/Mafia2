package com.awds333.a2016.mafia.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.dialogs.ExitDialog;
import com.awds333.a2016.mafia.dialogs.NamePickDialog;
import com.awds333.a2016.mafia.dialogs.ServNamePickDialog;

public class MainActivity extends Activity {
    Button b1, b2;
    DialogFragment naimDialog, servNameDialog, exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button)findViewById(R.id.newserver);
        b2 = (Button)findViewById(R.id.findeserv);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new server creating
                servNameDialog = new ServNamePickDialog();
                servNameDialog.show(getFragmentManager(),"mytag");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //locking for server
                naimDialog = new NamePickDialog();
                naimDialog.show(getFragmentManager(),"mytag");
            }
        });
    }

    @Override
    public void onBackPressed() {
        exit = new ExitDialog();
        exit.show(getFragmentManager(),"mytag");
    }
}
