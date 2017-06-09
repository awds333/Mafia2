package com.awds333.a2016.mafia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.engines.Engine;
import com.awds333.a2016.mafia.engines.PlayerEngine;
import com.awds333.a2016.mafia.engines.ServerEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;


public class PlayActivity extends Activity implements Observer{

    Engine engine;
    Handler handler;
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        context = this;
        if(getIntent().getExtras().getInt("type")==1){
            Bundle extras = getIntent().getExtras();
            engine = new ServerEngine(extras.getInt("mafia"),extras.getBoolean("doctor"),extras.getBoolean("detective"),extras.getString("name"),this);
        } else {
            engine = new PlayerEngine(this);
        }
        engine.addObserver(this);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1){
                    JSONObject message = (JSONObject) msg.obj;
                    try {
                        String type = message.getString("type");
                        Toast.makeText(context,type,Toast.LENGTH_LONG).show();
                        if(type.equals("connectionfail")){

                        } else if(type.equals("lespeople")){

                        } else if(type.equals("Iam")){
                            ((Button)findViewById(R.id.button)).setText(message.getInt("role")+"");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public void update(Observable o, Object arg) {
        Message message = handler.obtainMessage(1,arg);
        handler.obtainMessage();
        handler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        engine.finish();
        super.onDestroy();
    }
}
