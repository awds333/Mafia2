package com.awds333.a2016.mafia.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.server.WaitingForPlayersActivity;


public class ServNamePickDialog extends DialogFragment implements View.OnClickListener {
    View dialog;
    Activity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = inflater.inflate(R.layout.newserv_dialog, null);
        dialog.findViewById(R.id.yesbt).setOnClickListener(this);
        dialog.findViewById(R.id.canselbt).setOnClickListener(this);
        context = getActivity();
        return dialog;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.yesbt) {
            String name = ((EditText) dialog.findViewById(R.id.name)).getText().toString();
            String servname = ((EditText) dialog.findViewById(R.id.sname)).getText().toString();
            if (!name.replaceAll("\\s+", "").equals("")) {
                if (name.length() <= 20) {
                    if (!servname.replaceAll("\\s+", "").equals("")) {
                        if (servname.length() <= 20) {
                            Intent intent = new Intent(context, WaitingForPlayersActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("servername", servname);
                            context.startActivity(intent);
                            dismiss();
                        } else
                            Toast.makeText(context, context.getText(R.string.servnamemast), Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(context, context.getText(R.string.serventername), Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(context, context.getText(R.string.namemast), Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(context, context.getText(R.string.entername), Toast.LENGTH_LONG).show();
        } else {
            dismiss();
        }
    }
}
