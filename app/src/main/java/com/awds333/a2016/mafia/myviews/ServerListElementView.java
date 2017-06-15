package com.awds333.a2016.mafia.myviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.awds333.a2016.mafia.R;


public class ServerListElementView{
    private View view;
    private int ip;
    public ServerListElementView(Context context,int ip,int people,int lock, String serverName){
        view = LayoutInflater.from(context).inflate(R.layout.server_list_element, null);
        this.ip = ip;
        view.findViewById(R.id.join).setId(ip);
        ((TextView)view.findViewById(R.id.text)).setText(serverName);
        ((TextView)view.findViewById(R.id.playerscount)).setText(people+"");
        if (lock==0)
            ((ImageView)view.findViewById(R.id.lock)).setVisibility(View.INVISIBLE);
    }
    public View getView(){
        return view;
    }

    public int getIp() {
        return ip;
    }

    public  void setOnClick(View.OnClickListener listener){
        ((Button)view.findViewById(ip)).setOnClickListener(listener);
    }
}
