package com.awds333.a2016.mafia.engines;


import com.awds333.a2016.mafia.netclasses.SocketForPlayer;

public class PlayerEngine extends Engine {
    SocketForPlayer player;

    public PlayerEngine(){
        player = SocketForPlayer.getSocketForPlayer();
    }
}
