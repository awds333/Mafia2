package com.awds333.a2016.mafia.activities.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.MainActivity;
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
    boolean next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_game_start);
        context= this;
        next = false;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                player = SocketForPlayer.getSocketForPlayer();
                if (player.getSocket().isConnected()) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("type", "getPlayList");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    player.sendMessage(object.toString());
                    try {
                        player.getMessage();
                        String message = player.getMessage();
                        JSONObject plList = new JSONObject(message);
                        JSONArray array = plList.getJSONArray("PlayList");
                        mId = plList.getInt("id");
                        mName = plList.getString("name");
                        idName = new HashMap<Integer, String>();
                        for (int i =0; i<array.length();i++){
                            JSONObject ob = (JSONObject) array.get(i);
                            idName.put(ob.getInt("id"),ob.getString("name"));
                        }
                        start.sendEmptyMessage(1);
                    } catch (IOException e) {
                        start.sendEmptyMessage(0);
                    } catch (JSONException e) {
                        start.sendEmptyMessage(0);
                    }
                } else
                    start.sendEmptyMessage(0);
            }
        });
        start = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what== 1){
                    conteiner = (LinearLayout) findViewById(R.id.conteiner);
                    conteiner.removeView(findViewById(R.id.progressBar2));
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    Iterator<Integer> idi = idName.keySet().iterator();
                    while (idi.hasNext()){
                        int id = idi.next();
                        View view = LayoutInflater.from(context).inflate(R.layout.player_list_element, null);
                        view.setId(id);
                        ((TextView) view.findViewById(R.id.name)).setText(idName.get(id));
                        conteiner.addView(view,params);
                    }
                } else if (msg.what == 0){
                    Toast.makeText(context,context.getString(R.string.cooner),Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent(context, MainActivity.class));
                    context.finish();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onDestroy() {
        if(!next){

            player.close();
           /* Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(player!=null)
                        player.close();
                }
            });
            thread.start();*/
        }
        super.onDestroy();
    }
}
