package com.awds333.a2016.mafia.netclasses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerChannel {
    private Thread thread;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader reader;
    private String state;
    private int id;

    public PlayerChannel(Thread thread, Socket socket, int id) {
        this.thread = thread;
        this.socket = socket;
        this.id = id;
        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(this.socket.getOutputStream())),
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getMessage() throws IOException {
        return reader.readLine();
    }

    public void close() {
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
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
