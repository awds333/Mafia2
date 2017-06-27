package com.awds333.a2016.mafia.activities.client;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.MainActivity;
import com.awds333.a2016.mafia.activities.PlayActivity;
import com.awds333.a2016.mafia.dialogs.ConnectionErrorDialog;
import com.awds333.a2016.mafia.dialogs.ExitDialog;
import com.awds333.a2016.mafia.netclasses.SocketForPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class WaitingForGameStartActivity extends AppCompatActivity {
    Handler start;
    HashMap<Integer, String> idName;
    String mName;
    int mId;
    LinearLayout conteiner;
    LinearLayout.LayoutParams params;
    Activity context;
    SocketForPlayer player;
    boolean next, listen;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_game_start);
        context = this;
        next = false;
        listen = true;
        ((Button)findViewById(R.id.backbt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.onBackPressed();
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                player = SocketForPlayer.getSocketForPlayer();
                if (player.getSocket() == null)
                    start.sendEmptyMessage(0);
                else {
                    if (player.getSocket().isConnected()) {
                        JSONObject object = new JSONObject();
                        try {
                            object.put("type", "getPlayList");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(object.toString());
                        try {
                            JSONObject plList = new JSONObject();
                            while (true){
                                String message = player.getMessage();
                                plList = new JSONObject(message);
                                if(plList.getString("type").equals("fornew"))
                                    break;
                            }
                            JSONArray array = plList.getJSONArray("PlayList");
                            mId = plList.getInt("id");
                            mName = plList.getString("name");
                            idName = new HashMap<Integer, String>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject ob = (JSONObject) array.get(i);
                                idName.put(ob.getInt("id"), ob.getString("name"));
                            }
                            start.sendEmptyMessage(1);

                        } catch (IOException e) {
                            start.sendEmptyMessage(0);
                        } catch (JSONException e) {
                            start.sendEmptyMessage(0);
                        }
                        try {
                            while (listen) {
                                String mess = player.getMessage();
                                Message msg = start.obtainMessage(2, 0, 0, mess);
                                start.obtainMessage();
                                start.sendMessage(msg);
                                try {
                                    JSONObject ob = new JSONObject(mess);
                                    if(ob.getString("type").equals("gamestart"))
                                        listen=false;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            start.sendEmptyMessage(0);
                        }
                    } else
                        start.sendEmptyMessage(0);
                }
            }
        });
        start = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    conteiner = (LinearLayout) findViewById(R.id.conteiner);
                    conteiner.removeView(findViewById(R.id.progressBar2));
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    Iterator<Integer> idi = idName.keySet().iterator();
                    while (idi.hasNext()) {
                        int id = idi.next();
                        View view = LayoutInflater.from(context).inflate(R.layout.player_list_element, null);
                        view.setId(id);
                        ((TextView) view.findViewById(R.id.name)).setText(idName.get(id));
                        conteiner.addView(view, params);
                    }
                } else if (msg.what == 0) {
                    DialogFragment dialogFragment = new ConnectionErrorDialog();
                    ((ConnectionErrorDialog) dialogFragment).setListener(this);
                    if (listen)
                        dialogFragment.show(getFragmentManager(), "f");
                } else if (msg.what == 2) {
                    try {
                        JSONObject object = new JSONObject((String) msg.obj);
                        String type = object.getString("type");
                        if (type.equals("newPlayer")) {
                            idName.put(object.getInt("id"), object.getString("name"));
                            View view = LayoutInflater.from(context).inflate(R.layout.player_list_element, null);
                            view.setId(object.getInt("id"));
                            ((TextView) view.findViewById(R.id.name)).setText(object.getString("name"));
                            if(object.getBoolean("hasImage")){

                            }
                            conteiner.addView(view, params);
                        } else if (type.equals("connectionfail")) {
                            conteiner.removeView(conteiner.findViewById(object.getInt("id")));
                            idName.remove(object.getInt("id"));
                        } else if(type.equals("gamestart")){
                            JSONObject message = new JSONObject();
                            message.put("type","name");
                            message.put("name",mName);
                            player.sendMessage(message.toString());
                            next = true;
                            Intent intent = new Intent(context, PlayActivity.class);
                            intent.putExtra("id",mId);
                            intent.putExtra("type",2);
                            intent.putExtra("name",mName);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (msg.what == 3) {
                    startActivity(new Intent(context, MainActivity.class));
                    context.finish();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onDestroy() {
        listen = false;
        if (!next) {
            player.close();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DialogFragment exit = new ExitDialog();
        exit.show(getFragmentManager(), "mytag");
    }
}
