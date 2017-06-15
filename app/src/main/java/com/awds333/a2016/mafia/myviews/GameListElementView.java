package com.awds333.a2016.mafia.myviews;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.awds333.a2016.mafia.R;

public class GameListElementView {
    private View view;
    private int id;
    private int count;

    public GameListElementView(Context context, String name, int id, View.OnClickListener listener) {
        view = LayoutInflater.from(context).inflate(R.layout.game_liste_element, null);
        ((TextView) view.findViewById(R.id.name)).setText(name);
        ((Button) view.findViewById(R.id.choose)).setOnClickListener(listener);
        ((Button) view.findViewById(R.id.choose)).setId(id);
        count = 0;
        ((TextView) view.findViewById(R.id.count)).setText(count + "");
        this.id = id;
    }

    public View getView() {
        return view;
    }

    public int getId() {
        return id;
    }

    public void countUp() {
        count++;
        ((TextView) view.findViewById(R.id.count)).setText(count + "");
    }

    public void countZero(){
        count = 0;
        ((TextView) view.findViewById(R.id.count)).setText(count + "");
    }
}
