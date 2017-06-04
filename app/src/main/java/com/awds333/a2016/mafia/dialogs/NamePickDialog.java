package com.awds333.a2016.mafia.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.client.ServerSerchActivity;


public class NamePickDialog extends DialogFragment implements View.OnClickListener {
    View dialog;
    Activity context;
    SharedPreferences sPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = inflater.inflate(R.layout.name_dialog, null);
        dialog.findViewById(R.id.yesbt).setOnClickListener(this);
        dialog.findViewById(R.id.canselbt).setOnClickListener(this);
        context = getActivity();
        sPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        if(sPreferences.getBoolean("remember",false))
            ((EditText)dialog.findViewById(R.id.name)).setText(sPreferences.getString("name",""));
        return dialog;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.yesbt) {
            String s = ((EditText) dialog.findViewById(R.id.name)).getText().toString();
            if (!s.replaceAll("\\s+", "").equals("")) {
                Intent intent = new Intent(context, ServerSerchActivity.class);
                intent.putExtra("name", s);
                if(sPreferences.getBoolean("remember",false)) {
                    SharedPreferences.Editor editor = sPreferences.edit();
                    editor.putString("name", s);
                    editor.commit();
                }
                context.startActivity(intent);
                dismiss();
            } else
                Toast.makeText(context, context.getText(R.string.entername), Toast.LENGTH_LONG).show();
        } else {
            dismiss();
        }
    }
}
