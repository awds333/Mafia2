package com.awds333.a2016.mafia.engines;


import android.app.Activity;

import com.awds333.a2016.mafia.netclasses.SocketForPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PlayerEngine extends Engine {
    private SocketForPlayer player;
    private Activity context;
    private Thread listenThread;
    private boolean listen;

    public PlayerEngine(Activity activity) {
        player = SocketForPlayer.getSocketForPlayer();
        context = activity;
        listen = true;
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (listen) {
                        String message = player.getMessage();
                        handleMessage(message);
                    }
                } catch (IOException e) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("type","connectionfail");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    setChanged();
                    notifyObservers(object);
                }
            }
        });
        listenThread.start();
    }

    private void handleMessage(String message){
        try {
            JSONObject object = new JSONObject(message);
            setChanged();
            notifyObservers(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        listen=false;
        player.close();
    }
}
