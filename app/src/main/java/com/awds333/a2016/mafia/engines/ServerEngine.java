package com.awds333.a2016.mafia.engines;


import com.awds333.a2016.mafia.netclasses.SocketEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class ServerEngine extends Engine implements Observer {
    SocketEngine socketEngine;
    HashMap<Integer,String> idName;

    public ServerEngine(int mafia,boolean doctor,boolean detective,String name) {
        socketEngine = SocketEngine.getSocketEngine();
        socketEngine.addObserver(this);
        idName = new HashMap<Integer, String>();
        idName.put(0,name);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject gamestart = new JSONObject();
                try {
                    gamestart.put("type", "gamestart");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socketEngine.sendMessage(gamestart.toString());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        JSONObject news = (JSONObject) arg;
        try {
            String type = news.getString("type");
            if(type.equals("message")){
                JSONObject message = news.getJSONObject("message");
                if(message.getString("type").equals("name")){
                    idName.put(news.getInt("id"),message.getString("name"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
