package com.awds333.a2016.mafia.engines;


import android.app.Activity;
import android.util.Log;

import com.awds333.a2016.mafia.netclasses.SocketEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class ServerEngine extends Engine implements Observer {
    private SocketEngine socketEngine;
    private HashMap<Integer, String> idName;
    private ServerEngine me;
    private Activity context;
    private boolean detective,doctor;
    private int mafia;

    public ServerEngine(int mfia,boolean dctor, boolean dtective, String name, Activity activity) {
        socketEngine = SocketEngine.getSocketEngine();
        socketEngine.addObserver(this);
        this.detective = dtective;
        this.doctor = dctor;
        this.mafia = mfia;
        idName = new HashMap<Integer, String>();
        idName.put(0, name);
        context = activity;
        me = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject gamestart = new JSONObject();
                    gamestart.put("type", "gamestart");

                    socketEngine.sendMessage(gamestart.toString());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("awdsawds",idName.toString());
                    if (idName.size() < 4) {
                        JSONObject problem = new JSONObject();
                        problem.put("type","lespeople");
                        socketEngine.sendMessage(problem.toString());
                        setChanged();
                        notifyObservers(problem);
                    } else {
                        int roles[] = createRolesList();
                        Log.d("awdsawds",roles[0]+" "+roles[1]+roles[2]+roles[3]);
                        telRoles(roles);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("awdsawds","fail");
                }
            }
        });
        thread.start();
    }

    private void telRoles(int roles[]) throws JSONException {
        Iterator<Integer> iterator = idName.keySet().iterator();
        for(int i = 0;i<roles.length;i++){
            int id = iterator.next();
            JSONObject role = new JSONObject();
            if(id == 0){
                role.put("type","Iam");
                role.put("role",roles[i]);
                role.put("people",idName.toString());
                Log.d("awdsawds", role.toString());
                Log.d("awdsawds", id + "k");
                setChanged();
                notifyObservers(role);
            } else {
                role.put("type", "Iam");
                role.put("role", roles[i]);
                role.put("people",idName.toString());
                Log.d("awdsawds", role.toString());
                Log.d("awdsawds", id + "");
                socketEngine.sendMessageById(role.toString(), id);
            }
        }
    }

    private int[] createRolesList(){
        int roles[] = new int[idName.size()];
        Random random = new Random();
        for (int i = 0;i<roles.length;i++)
            roles[i] = -1;
        for (int i = 0;i<mafia;i++){
            int n = random.nextInt(roles.length);
            if(roles[n]!=1)
                roles[n]=1;
            else
                i--;
        }
        if(detective)
            while (detective){
                int n = random.nextInt(roles.length);
                if(roles[n]==-1) {
                    roles[n] = 2;
                    detective = false;
                }
            }
        if(doctor) {
            while (doctor) {
                int n = random.nextInt(roles.length);
                if (roles[n] == -1) {
                    roles[n] = 3;
                    doctor = false;
                }
            }
            boolean beach = true;
            while (beach) {
                int n = random.nextInt(roles.length);
                if (roles[n] == -1) {
                    roles[n] = 4;
                    beach = false;
                }
            }
        }
        return roles;
    }

    @Override
    public void finish() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                socketEngine.stopPing();
                socketEngine.deleteObserver(me);
                socketEngine.finish();
            }
        });
        thread.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        JSONObject news = (JSONObject) arg;
        try {
            String type = news.getString("type");
            if (type.equals("message")) {
                JSONObject message = new JSONObject(news.getString("message"));
                if (message.getString("type").equals("name")) {
                    idName.put(news.getInt("id"), message.getString("name"));
                }
            } else if(type.equals("connectionfail")){
                JSONObject object = new JSONObject();
                object.put("id",news.getInt("id"));
                object.put("type","playergone");
                setChanged();
                notifyObservers(object);
                socketEngine.sendMessage(object.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
