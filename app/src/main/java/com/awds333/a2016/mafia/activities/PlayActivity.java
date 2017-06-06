package com.awds333.a2016.mafia.activities;

import android.app.Activity;
import android.os.Bundle;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.engines.Engine;
import com.awds333.a2016.mafia.engines.PlayerEngine;
import com.awds333.a2016.mafia.engines.ServerEngine;

import java.util.Observable;
import java.util.Observer;


public class PlayActivity extends Activity implements Observer{

    Engine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        if(getIntent().getExtras().getInt("type")==1){
            Bundle extras = getIntent().getExtras();
            engine = new ServerEngine(extras.getInt("mafia"),extras.getBoolean("doctor"),extras.getBoolean("detective"),extras.getString("name"));
        } else {
            engine = new PlayerEngine();
        }
        engine.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
