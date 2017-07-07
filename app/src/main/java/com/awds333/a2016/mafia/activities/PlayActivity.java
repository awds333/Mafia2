package com.awds333.a2016.mafia.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.dialogs.ConnectionErrorDialog;
import com.awds333.a2016.mafia.engines.Engine;
import com.awds333.a2016.mafia.engines.PlayerEngine;
import com.awds333.a2016.mafia.engines.ServerEngine;
import com.awds333.a2016.mafia.myviews.GameListElementView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class PlayActivity extends Activity implements Observer {

    Engine engine;
    Handler handler;
    Activity context;
    HashMap<Integer, String> idName;
    LinearLayout container;
    ArrayList<GameListElementView> players;
    int myId, myRole;
    LinearLayout roleContainer;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        context = this;
        Bundle extras = getIntent().getExtras();
        if (getIntent().getExtras().getInt("type") == 1) {
            engine = new ServerEngine(extras.getInt("mafia"), extras.getBoolean("doctor"), extras.getBoolean("detective"), extras.getString("name"), this);
        } else {
            engine = new PlayerEngine(this);
        }
        engine.addObserver(this);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    JSONObject message = (JSONObject) msg.obj;
                    try {
                        String type = message.getString("type");
                        Toast.makeText(context, type, Toast.LENGTH_LONG).show();
                        if (type.equals("playergone")) {
                            int id = message.getInt("id");
                            for (int i = 0; i < players.size(); i++) {
                                if (players.get(i) != null)
                                    if (players.get(i).getId() == id) {
                                        container.removeView(players.get(i).getView());
                                        players.remove(players.get(i));
                                    }
                            }
                        } else if (type.equals("connectionfail")) {
                            ConnectionErrorDialog dialog = new ConnectionErrorDialog();
                            dialog.setListener(handler);
                            if (!context.isDestroyed())
                                dialog.show(getFragmentManager(), "MyTag");
                        } else if (type.equals("lespeople")) {

                        } else if (type.equals("Iam")) {
                            myRole = message.getInt("role");
                            idName = new HashMap<Integer, String>();
                            String people = message.getString("people");
                            String[] pairs = people.substring(1, people.length() - 1).split(", ");
                            for (int i = 0; i < pairs.length; i++) {
                                String pair = pairs[i];
                                String[] keyValue = pair.split("=" +
                                        ".");
                                idName.put(Integer.valueOf(keyValue[0]), keyValue[1]);
                            }
                            fillContainer();
                            showRole();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (msg.what == 3) {
                    finish();
                }
            }
        };
        ((TextView) findViewById(R.id.my_name)).setText(extras.getString("name"));
        myId = extras.getInt("id", 0);
        timer = new Timer();
    }

    public void showRole() {
        TextView view = new TextView(this);
        view.setText(R.string.hide_screen);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        view.setMaxWidth(container.getWidth());
        roleContainer.addView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            roleContainer.setBackgroundColor(Color.GRAY);
            view.setTextColor(Color.BLACK);
        } else {
            roleContainer.setBackgroundColor(Color.GRAY);
            view.setTextColor(Color.BLACK);
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        roleContainer.removeAllViews();
                        roleContainer.setBackgroundColor(Color.WHITE);
                        ImageView imageView = new ImageView(context);
                        if (myRole == -1) {
                            imageView.setImageResource(R.drawable.innocent);
                        } else if (myRole == 1) {
                            Random random = new Random();
                            int mf = random.nextInt(3);
                            if (mf == 0)
                                imageView.setImageResource(R.drawable.mafia1);
                            else if (mf == 1)
                                imageView.setImageResource(R.drawable.mafia2);
                            else if (mf == 2)
                                imageView.setImageResource(R.drawable.mafia3);
                        } else if (myRole == 2) {
                            imageView.setImageResource(R.drawable.commissar);
                        } else if (myRole == 3) {
                            imageView.setImageResource(R.drawable.doctor);
                        } else if (myRole == 4) {
                            imageView.setImageResource(R.drawable.prostitute);
                        }
                        roleContainer.addView(imageView);
                        TimerTask tmTask = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        roleContainer.removeAllViews();
                                    }
                                });
                            }
                        };
                        timer.schedule(tmTask, 5000);
                    }
                });
            }
        };
        timer.schedule(task, 5000);
    }

    public void fillContainer() {
        roleContainer = (LinearLayout) findViewById(R.id.role_container);
        roleContainer.removeAllViews();
        container = (LinearLayout) findViewById(R.id.container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 0);
        players = new ArrayList<GameListElementView>();
        Iterator<Integer> ids = idName.keySet().iterator();
        ArrayList<Integer> intIds = new ArrayList<Integer>();
        while (ids.hasNext()) {
            int id = ids.next();
            intIds.add(id);
            if (id != myId) {
                GameListElementView view = new GameListElementView(this, idName.get(id), id, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, v.getId() + "", Toast.LENGTH_LONG).show();
                    }
                });
                players.add(view);
                container.addView(view.getView(), params);
            }
        }
        AsyncTask<ArrayList<Integer>,Bitmap,Bitmap> setImages = new AsyncTask<ArrayList<Integer>, Bitmap, Bitmap>() {
            int id;
            @Override
            protected Bitmap doInBackground(ArrayList<Integer>... params) {
                for(int i =0;i<params[0].size();i++){
                    byte imageBytes[] = getIntent().getByteArrayExtra("image"+params[0].get(i));
                    if(imageBytes!=null){
                        Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        id = params[0].get(i);
                        publishProgress(bmp.copy(Bitmap.Config.ARGB_8888, true));
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Bitmap... values) {
                View view = findViewById(id+1000);
                if(view!=null){
                    ((ImageView)view.findViewById(R.id.image)).setImageBitmap(values[0]);
                }
            }
        }.execute(intIds);
    }

    @Override
    public void update(Observable o, Object arg) {
        Message message = handler.obtainMessage(1, arg);
        handler.obtainMessage();
        handler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        engine.finish();
        super.onDestroy();
    }
}
