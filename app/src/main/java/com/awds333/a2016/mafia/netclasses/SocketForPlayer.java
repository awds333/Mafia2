package com.awds333.a2016.mafia.netclasses;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketForPlayer {

    private static SocketForPlayer player;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter out;

    private SocketForPlayer() {
    }

    public static SocketForPlayer getSocketForPlayer() {
        if (player == null)
            player = new SocketForPlayer();
        return player;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(this.socket.getOutputStream())),
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() throws IOException {
        return reader.readLine();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() {
        Log.d("awdsawds","t");
        if (out != null) {
            out.close();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
