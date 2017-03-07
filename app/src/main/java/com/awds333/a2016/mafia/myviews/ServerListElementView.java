package com.awds333.a2016.mafia.myviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.awds333.a2016.mafia.R;

/**
 * Created by Usre on 20.01.2017.
 */

public class ServerListElementView{
    private View view;
    private int ip;
    public ServerListElementView(Context context,int ip,int people, String serverName){
        view = LayoutInflater.from(context).inflate(R.layout.server_list_element, null);
        this.ip = ip;
        ((TextView)view.findViewById(R.id.name)).setText(serverName);
        ((TextView)view.findViewById(R.id.playerscount)).setText(people+"");
    }
    public View getView(){
        return view;
    }

    public int getIp() {
        return ip;
    }
}
